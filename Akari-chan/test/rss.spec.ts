import * as assert from 'assert';
import { RssFeed } from '../src/lib/scribblehub/rss';

describe('author rss', function () {
    it('should have all the data from the rss.', async function () {
        const rss = await RssFeed.fromUser(24680)

        assert.notEqual(rss.lastBuildDate, null, 'The last build date of the feed is null.')
        assert.notEqual(rss.chapters, null, 'The chapter list of the feed is null.')
        assert.notEqual(rss.chapters.length, 0, 'The chapter list of the feed is empty.')
        
        rss.chapters.forEach(chapter => {
            assert.notEqual(chapter.link, null, 'A chapter link is null.')
            assert.notEqual(chapter.pubDate, null, 'A chapter published date is null.')
            assert.notEqual(chapter.title, null, 'A chapter title is null.')
            assert.notEqual(chapter.story.creator, null, 'A chapter story\'s creator is null.')
            assert.notEqual(chapter.story.title, null, 'A chapter story\'s title is null.')
        });
    });
});

describe('story rss', function () {
    it('should have all the data from the rss.', async function () {
        const rss = await RssFeed.fromSeries(299262)

        assert.notEqual(rss.lastBuildDate, null, 'The last build date of the feed is null.')
        assert.notEqual(rss.chapters, null, 'The chapter list of the feed is null.')
        assert.notEqual(rss.chapters.length, 0, 'The chapter list of the feed is empty.')
        
        rss.chapters.forEach(chapter => {
            assert.notEqual(chapter.link, null, 'A chapter link is null.')
            assert.notEqual(chapter.pubDate, null, 'A chapter published date is null.')
            assert.notEqual(chapter.title, null, 'A chapter title is null.')
            assert.notEqual(chapter.story.creator, null, 'A chapter story\'s creator is null.')
            assert.notEqual(chapter.story.title, null, 'A chapter story\'s title is null.')
        });
    });
});
