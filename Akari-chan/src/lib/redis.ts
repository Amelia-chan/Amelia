import * as redis from 'redis';
import configuration from './configuration';

export default redis.createClient({
    url: configuration('REDIS_URI')
})