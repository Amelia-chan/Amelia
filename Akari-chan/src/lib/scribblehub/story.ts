import axios from 'axios';
import { decode } from 'html-entities';
import { HTMLElement, parse } from 'node-html-parser';
import { SCRIBBLEHUB_TRENDING_RESET } from '../dates';
import redis from '../redis';
import requests from '../requests';

// The amount of time that we should cache the story informations. This is best kept at 
// a week or even a month since this is used by the RSS feed module to get the author name
// from the story (since who knew Story RSS feeds returned author names).
//
// This is kept at a week for safety reasons (like when an author changes their name).
const STORY_CACHE_TIME = 
    60 * // 1 minute
    60 * // 1 hour
    24 * // 1 day
    7    // 7 days
    ;

// ScribbleHub has a known bug that isn't going to be fixed anytime soon where you need to include 
// a slug at the end of the url. I don't know if this is a WordPress behavior or something but to circumvent
// the issue, all we need to is fake a slug at the end.
const url = (id: number) => `https://www.scribblehub.com/series/${id}/amelia-akari-chan`;

const TRENDING_URL = "https://www.scribblehub.com/series-ranking/?sort=5&order=1";

// We'll be relying on the fact that ScribbleHub doesn't change much to directly access
// the fields that we want through their classnames or identifiers.
const AUTHOR_FIELD = ".auth_name_fic";
const TITLE_FIELD = ".fic_title";
const COVER_FIELD = ".fic_image > img";

const TRENDING_STORY_CONTAINER = ".search_main_box";
const TRENDING_STORY_COVER = ".search_img:first-child > img";
const TRENDING_STORY_TITLE = ".search_title > a";
const TRENDING__STORY_AUTHOR = '.nl_stat[title="Author"] > .a_un_st > a';

export class Story {

    id: number;
    title: string;
    author: string;
    cover: string | undefined;

    constructor(id: number, title: string, author: string, cover: string | undefined) {
        this.id = id;
        this.title = decode(title);
        this.author = decode(author);
        this.cover = cover;
    }

    /**
     * Requests the story information from our Redis cache.
     * 
     * @param id The ID of the story to request.
     * @returns The {@link Story} from our Redis cache if present.
     */
    private static async cache(id: number): Promise<Story | null> {
        return redis.get(encodeURI(url(id))).then(result => {
            if (result == null) return null;

            const story = JSON.parse(result);
            return new Story(story.id, story.title, story.author, story.cover);
        })
    }

    /**
     * Puts the story information into the Redis cache up to {@link STORY_CACHE_TIME} to reduce 
     * the total amount of time it takes for a story feed to be received and also to reduce amount of 
     * requests to ScribbleHub.
     * 
     * @param story The {@link Story} to store into the cache.
     * @returns The {@link Story} provided in the parameters.
     */
    private static async put(story: Story) {
        redis.setEx(encodeURI(url(story.id)), STORY_CACHE_TIME, JSON.stringify(story));

        return story;
    }

    /**
     * Requests a story's information from ScribbleHub. The result from this method is cached up to {@link STORY_CACHE_TIME}.
     * 
     * @param id The id of the story to request from ScribbleHub.
     * @returns The {@link Story} from ScribbleHub.
     */
    public static async withId(id: number): Promise<Story> {
        if (redis.isOpen) {
            const cached = await Story.cache(id);

            if (cached) {
                return cached;
            }
        }

        return axios.get(url(id), {
            headers: requests.headers
        }).then((response) => {
            if (response.status === 404) {
                throw {
                    response: {
                        status: 404
                    }
                }
            }

            const tree = parse(response.data);

            const title = tree.querySelector(TITLE_FIELD)!.innerText;
            const author = tree.querySelector(AUTHOR_FIELD)!.innerText;
            const cover = tree.querySelector(COVER_FIELD)!.attributes.src!;

            return new Story(id, title, author, cover);
        }).then(story => {
            if (redis.isOpen) {
                return Story.put(story)
            }

            return story
        })
    }

    /**
     * Requests the current trending stories from the ScribbleHub website. This is cached till the next GMT+0 1:00 time 
     * which is the best time to refresh the data from ScribbleHub.
     * 
     * @returns All the stories that have reached trending at this moment.
     */
    public static async trending(): Promise<Story[]> {
        if (redis.isOpen) {
            const cached = await redis.get('trending').then(result => {
                if (!result) return null;
    
                return JSON.parse(result);
            });
    
            if (cached) {
                return cached;
            }
        }

        return axios.get(TRENDING_URL, {
            headers: requests.headers
        }).then((response) => {
            const tree = parse(response.data);

            const stories: HTMLElement[] = tree.querySelectorAll(TRENDING_STORY_CONTAINER);
            return stories.map(story => {
                const titleAndId = story.querySelector(TRENDING_STORY_TITLE)!;
                const author = story.querySelector(TRENDING__STORY_AUTHOR)!.innerText;

                const cover = story.querySelector(TRENDING_STORY_COVER)!.attributes.src!;

                const id = Number.parseInt(titleAndId.attributes.href.replace('https://', '').split('/')[2])!;

                return new Story(id, titleAndId.innerText, author, cover);
            })
        }).then(stories => {
            if (redis.isOpen) {
                const cacheTill = SCRIBBLEHUB_TRENDING_RESET();
                redis.setEx('trending', Math.floor((cacheTill.valueOf() - new Date().valueOf()) /1000), JSON.stringify(stories));
            }

            return stories;
        });
    }

}