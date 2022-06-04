import * as assert from 'assert';
import { Story } from '../src/lib/scribblehub/story';

describe('story trending', async function () {
    
    // Please note that all of these values are vulnerable to change.
    // If I do change the title or author name of my account or story then please 
    // change a PR to match this if I didn't already.
    it('should contain all the trending stories.', async function () {
        const trending = await Story.trending()

        trending.forEach(story => {
            assert.notEqual(story.title, null, "A trending story title is somehow not present.")
            assert.notEqual(story.cover, null, "A trending story cover is somehow not present.")
            assert.notEqual(story.id, null, "A trending story id is somehow not present.")
            assert.notEqual(story.author, null, "A trending story author is somehow not present.")
            assert.equal(story.cover!.startsWith('https://cdn.scribblehub.com/seriesimg/'), true, 'The cover should start with the standard cover link of ScribbleHub.')
        });
    });
});