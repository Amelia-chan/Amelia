import dotenv from 'dotenv';

dotenv.config();

export default (key: string) => {
    if (process.env[key] == null) {
        dotenv.config();
    }  

    return process.env[key];
}