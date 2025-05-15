// functions/server.js
const serverless = require('serverless-http');
const app        = require('../server');   // the refactored Express app
module.exports = { handler: serverless(app) };
