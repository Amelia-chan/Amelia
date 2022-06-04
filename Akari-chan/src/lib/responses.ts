import Koa from 'koa';
import logger from './logger';
import { RssFeed } from './scribblehub/rss';

export class AkariResponder {

    /**
     * Receives and handles the HTTP request from Koa for RSS Feeds. This ensures that all RSS routes indeed meets the given 
     * RSS route specifications of Akari-chan.
     * 
     * @param context The {@link Koa.ParameterizedContext} received from Koa.
     * @param next The {@link Koa.next} received from Koa.
     * @param promise A promise to return the {@link RssFeed} that meets the context of the request.
     * @returns A response to the HTTP request.
     */
    public static async feed(context: Koa.ParameterizedContext, next: Koa.Next, promise: (id: number) => Promise<RssFeed>): Promise<any> {
        if (Number.isNaN(context.params.id)) {
            context.body = JSON.stringify({
                error: "The request does not meet the :id specifications."
            });
            context.status = 400;
            return;
        }

        const id = Number.parseInt(context.params.id);
        return promise(id).then(feed => {
            const newBuildDate = Buffer.from(feed.lastBuildDate.toString(), 'utf8').toString('base64url');

            if (!context.query.after) {
                context.body = JSON.stringify({
                    ...feed,
                    'after': newBuildDate
                });
                return next();
            }

            const lastBuildDate = new Date(Buffer.from(context.query.after as string, 'base64url').toString());
            context.body = JSON.stringify({
                ...feed.after(lastBuildDate),
                'after': newBuildDate
            })

            return next();
        }).catch((err: any) => {
            if (err.response && err.response.status && err.response.status === 404) {
                context.body = JSON.stringify({
                    error: "There is no available data on this identifier."
                });
                
                context.status = 404;

                logger.warn({
                    address: context.ip,
                    url: context.URL.toString(),
                    error: "There is no entity on the given identifier, please be warned of spam requests."
                });
                return next();
            }

            console.error(err);
            context.body = JSON.stringify({
                error: "Akari-chan is unable to handle this request at the moment, if you are the admin then please check the console."
            });

            context.status = 500;
            return next();
        });
    }

}