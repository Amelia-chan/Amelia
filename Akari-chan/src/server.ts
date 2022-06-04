import Router from "@koa/router";
import Koa from "koa";
import configuration from "./lib/configuration";
import { SCRIBBLEHUB_TRENDING_RESET } from "./lib/dates";
import logger from "./lib/logger";
import { AkariResponder } from "./lib/responses";
import { RssFeed } from "./lib/scribblehub/rss";
import { Story } from "./lib/scribblehub/story";

const server: Koa = new Koa({
    proxy: configuration('PROXY') === 'true' ?? false
})

const router = new Router()

router.get('/user/:id', async (context, next) => AkariResponder.feed(context, next, id => RssFeed.fromUser(id)));
router.get('/series/:id', async (context, next) => AkariResponder.feed(context, next, id => RssFeed.fromSeries(id)));
router.get('/trending', async (context) => {
    return Story.trending().then(stories => {
        context.body = JSON.stringify({
            cachedUntil: SCRIBBLEHUB_TRENDING_RESET(),
            stories: stories
        });
    })
});
router.get('/', (context) => {
    context.body = JSON.stringify({
        message: "Oh, hello there! It seems like you found Akari-chan who should be hiding under a firewall.",
        github: "https://github.com/Amelia-chan/Amelia"
    })
});

server.use((context, next) => {
    logger.info({
        method: context.method,
        url: context.URL.toString(),
        params: context.params,
        query: context.query,
        address: context.ip
    });

    context.set({
        'Content-Type': 'application/json'
    });

    return next();
});

server.use(router.routes()).use(router.allowedMethods());

export function start() {
    server.listen(5056, () => {
        logger.info({
            port: 5056,
            message: "The server is now accepting requests."
        })
    })
}