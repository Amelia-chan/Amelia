
/**
 * Gets the guaranteed, finalized trending reset time of ScribbleHub. ScribbleHub is known to reset trending at GMT+0 0:00 which is 
 * the standard next day for servers, but since there are too many entities to calculate, the best time to collect the latest trending
 * results is always an hour after.
 * 
 * Therefore, Akari-chan only refreshes the results at that time.
 * 
 * @returns The guaranteed finalized trending reset time of ScribbleHub. It is expected that this value should be 
 * at 1:00 GMT+0.
 */
export function SCRIBBLEHUB_TRENDING_RESET() {
    const nextDay = new Date(new Date().toUTCString());
    nextDay.setUTCDate(nextDay.getDate() + 1);
    nextDay.setUTCSeconds(0);
    nextDay.setUTCHours(1);
    nextDay.setUTCMilliseconds(0);
    nextDay.setUTCMinutes(0);

    return nextDay;
}