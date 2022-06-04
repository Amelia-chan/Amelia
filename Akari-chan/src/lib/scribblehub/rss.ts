import axios from "axios";
import { XMLParser } from "fast-xml-parser";
import requests from "../requests";
import { Story } from "./story";
import redis from '../redis';
import logger from "../logger";

const feedUrl = (type: "series" | "author", id: number): string => {
    return (`https://www.scribblehub.com/rssfeed.php?type=${type}&${type === 'series' ? 'sid' : 'uid'}=${id}`)
}

// The amount of time that we should cache the RSS results. I personally think this is best 
// kept at 5 minutes since it's common for Amelia to contain multiple of the same feed for different 
// channels or servers which means multiple hits to the same feed.
const CACHE_TIME = 5 * 60;

const PARSER = new XMLParser();

export class RssFeed {
    lastBuildDate: Date;
    chapters: RssChapter[];

    constructor(lastBuildDate: Date, chapters: RssChapter[]) {
        this.lastBuildDate = lastBuildDate;
        this.chapters = chapters;
    }

    public after(date: Date): RssFeed {
        return new RssFeed(this.lastBuildDate, this.chapters.filter(chapter => chapter.pubDate.getTime() > date.getTime()));
    }

    public static async cache(type: "series" | "author", id: number): Promise<RssFeed | null> {
        return redis.get(encodeURI(feedUrl(type, id))).then(result => {
            if (result == null) return null;

            const feed = JSON.parse(result);
            return new RssFeed(new Date(feed.lastBuildDate), feed.chapters)
        });
    }

    private static put(type: "series" | "author", id: number, feed: RssFeed): RssFeed {
        if (redis.isOpen) {
            redis.setEx(encodeURI(feedUrl(type, id)), CACHE_TIME, JSON.stringify(feed));
        }
        
        return feed;
    }

    public static async fromUser(id: number) {
        if (redis.isOpen) {
            const cached = await RssFeed.cache('author', id)

            if (cached) {
                logger.info({
                    message: "A request has been served from cache.",
                    type: "author",
                    id: id
                })
                return cached;
            }
        }
        return axios.get(feedUrl('author', id), {
            headers: requests.headers
        }).then(response => {
            const tree = PARSER.parse(response.data).rss.channel;

            if (!tree.item) {
                throw {
                    response: {
                        status: 404
                    }
                }
            }
            const chapters: RssChapter[] = (tree.item as any[]).map(chapter => {
                return {
                    title: chapter.title,
                    link: chapter.link,
                    pubDate: new Date(chapter.pubDate),
                    story: {
                        title: chapter.category[0],
                        creator: chapter['dc:creator']
                    }
                }
            });

            return new RssFeed(new Date(tree.lastBuildDate), chapters);
        }).then(feed => RssFeed.put('author', id, feed));
    }

    public static async fromSeries(id: number) {
        if (redis.isOpen) {
            const cached = await RssFeed.cache('series', id)

            if (cached) {
                logger.info({
                    message: "A request has been served from cache.",
                    type: "series",
                    id: id
                })
                return cached;
            }
        }

        return axios.get(feedUrl('series', id), {
            headers: requests.headers
        }).then(async (response) => {
            const tree = PARSER.parse(response.data).rss.channel;

            if (!tree.item) {
                throw {
                    response: {
                        status: 404
                    }
                }
            }

            const author = await Story.withId(id).then(story => story.author)

            const chapters: RssChapter[] = (tree.item as any[]).map(chapter => {
                return {
                    title: chapter.title,
                    link: chapter.link,
                    pubDate: new Date(chapter.pubDate),
                    story: {
                        title: chapter.category[0],
                        creator: author
                    }
                }
            });

            let lastBuildDate: Date = new Date(new Date().toUTCString());

            if (chapters.length > 0) {
                lastBuildDate = chapters[0].pubDate;
            }

            return new RssFeed(lastBuildDate, chapters);
        }).then(feed => RssFeed.put('series', id, feed));
    }
}

export interface RssChapter {
    title: string,
    link: string,
    pubDate: Date,
    story: RssStory
}

export interface RssStory {
    title: string,
    creator: string
}