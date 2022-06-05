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

    /**
     * Filters all the chapters that was published before or equals the date provided, leaving only the 
     * latest chapters available to the end-user.
     * 
     * @param date The date to reference for filtering.
     * @returns A new {@link RssFeed} instance but with only the chapters that matches the filter.
     */
    public after(date: Date): RssFeed {
        return new RssFeed(this.lastBuildDate, this.chapters.filter(chapter => chapter.pubDate.getTime() > date.getTime()));
    }

    /**
     * Requests a cached value from Redis to ensure performance and reduce the risk of accidental or intentional DoS attacks 
     * towards ScribbleHub.
     * 
     * @param type The type of entity to request from Redis.
     * @param id The id of the entity to request from Redis.
     * @returns The value returned from Redis if there is any, this is mapped to an {@link RssFeed} when
     * there is a value.
     */
    public static async cache(type: "series" | "author", id: number): Promise<RssFeed | null> {
        return redis.get(encodeURI(feedUrl(type, id))).then(result => {
            if (result == null) return null;

            const feed = JSON.parse(result);

            const chapters: RssChapter[] = (feed.chapters as any[]).map(chapter => {
                return {
                    title: chapter.title,
                    link: chapter.link,
                    pubDate: new Date(chapter.pubDate),
                    story: {
                        title: chapter.story.title,
                        creator: chapter.story.creator
                    }
                }
            })
            
            return new RssFeed(new Date(feed.lastBuildDate), chapters)
        });
    }

    /**
     * Puts a {@link RssFeed} onto the Redis cache to be cached up to {@link CACHE_TIME}.
     * 
     * @param type The type of entity to store into Redis.
     * @param id The id of the entity to store into Redis.
     * @param feed The {@link RssFeed} to store into the Redis cache.
     * @returns The same {@link RssFeed} provided in the parameters, reflected.
     */
    private static put(type: "series" | "author", id: number, feed: RssFeed): RssFeed {
        if (redis.isOpen) {
            redis.setEx(encodeURI(feedUrl(type, id)), CACHE_TIME, JSON.stringify(feed));
        }

        return feed;
    }

    /**
     * Requests from ScribbleHub the RSS feed of the author or user provided. This will cache the result 
     * up to {@link CACHE_TIME} with Redis which means you could be getting outdated results if anything changes
     * within those 5 minutes.
     * 
     * @param id The ID of the user to request from ScribbleHub.
     * @returns The full {@link RssFeed} contents of the user from ScribbleHub, this can be 
     * cached by Redis up to {@link CACHE_TIME}.
     */
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

    /**
     * Requests from ScribbleHub the RSS feed of the series provided. This will cache the result 
     * up to {@link CACHE_TIME} with Redis which means you could be getting outdated results if anything changes
     * within those 5 minutes.
     * 
     * @param id The ID of the series to request from ScribbleHub.
     * @returns The full {@link RssFeed} contents of the series from ScribbleHub, this can be 
     * cached by Redis up to {@link CACHE_TIME}.
     */
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