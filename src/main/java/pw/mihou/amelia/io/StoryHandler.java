package pw.mihou.amelia.io;

public class StoryHandler {


    /**
     * This is created to address an issue with RSS for Series
     * not having any author.
     *
     * @param syndAuthor the default result from SyndAuthor.
     * @param id         the ID of the story.
     * @return the author.
     */
    public static String getAuthor(String syndAuthor, int id, String url) {


        if (syndAuthor.isEmpty() || syndAuthor.isBlank()) {
            if (url.contains("author"))
                return AmatsukiWrapper.getUserById(id).getName();

            return AmatsukiWrapper.getStoryById(id).getCreator();
        }

        return syndAuthor;
    }

}
