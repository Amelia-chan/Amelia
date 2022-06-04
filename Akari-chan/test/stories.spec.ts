import * as assert from 'assert';
import { Story } from '../src/lib/scribblehub/story';

describe('story get', async function () {
    
    // Please note that all of these values are vulnerable to change.
    // If I do change the title or author name of my account or story then please 
    // change a PR to match this if I didn't already.
    it('should match all the information given on the series page.', async function () {
        const story = await Story.withId(299262);

        assert.equal(story.title, 'The Vampire Empress', 'The title should match what is given on the series page.')
        assert.equal(story.cover!.startsWith('https://cdn.scribblehub.com/images/'), true, 'The cover should start with the standard cover link of ScribbleHub.')
        assert.equal(story.author, 'Mihou', 'The author of the story should match with what is given on the series page.')
        assert.equal(story.id, 299262, 'The series identifier should be equals to the given identifier.')
    });
});