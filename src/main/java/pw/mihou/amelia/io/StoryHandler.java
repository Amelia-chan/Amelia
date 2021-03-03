package pw.mihou.amelia.io;

public class StoryHandler {


    /**
     * This is created to address an issue with RSS for Series
     * not having any author.
     * @param syndAuthor the default result from SyndAuthor.
     * @param id the ID of the story.
     * @return the author.
     */
    public static String getAuthor(String syndAuthor, int id){

        if(syndAuthor.isEmpty() || syndAuthor.isBlank()) {
            return AmatsukiWrapper.getStoryById(id).getCreator();
        }

        return syndAuthor;
    }

}
