import redis from "./lib/redis";
import { start } from "./server";

redis.on('error', (err) => {
    console.error("An error occurred over at Redis: ", err);
});

(async () => {
    await connect();
    start();
})();

async function connect(): Promise<void> {
    try {
        await redis.connect();
    } catch (err: any) {
        console.error("An error occurred while trying to connect to all services, retrying in 1.5 seoconds...", err);
        
        await new Promise(resolve => setTimeout(resolve, 1500));
        return connect();
    }
}