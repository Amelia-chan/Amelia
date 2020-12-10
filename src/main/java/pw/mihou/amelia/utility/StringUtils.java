package pw.mihou.amelia.utility;

public class StringUtils {

    /**
     * Strips the string into the character limit, not including the spacing.
     * @param content the content to strip.
     * @param length the maximum length.
     * @return String.
     */
    public static String stripToLength(String content, int length){
        return content.replaceAll(" ", "").length() > length ? content.substring(0, length) : content;
    }

    /**
     * Strips the string into the character limit, whilst counting spacing.
     * @param content the content to strip.
     * @param length the maximum length.
     * @return String.
     */
    public static String stripToLengthWithSpacing(String content, int length){
        return content.length() > length ? content.substring(0, length) : content;
    }

    /**
     * Strips the string into the character limit, not including the spacing and ends it with a triple dot.
     * @param content the content to strip.
     * @param length the maximum length.
     * @return String.
     */
    public static String stripToLengthWhileDotsEnd(String content, int length){
        return content != null ? (content.replaceAll(" ", "").length() > length ? content.substring(0, length) + "..." : content) : "No context.";
    }

}
