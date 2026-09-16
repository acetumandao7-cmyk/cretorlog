const fs = require("fs");
const mysql = require("mysql2/promise");
require("dotenv").config();

async function importBackup() {
    let connection;

    try {
        console.log("Reading backup file...");

        const buffer = fs.readFileSync("./creatorlog_backup.sql");

        const sql = buffer
            .toString("utf16le")
            .replace(/^\uFEFF/, "");

        console.log("Backup file loaded.");
        console.log("Connecting to Aiven MySQL...");

        connection = await mysql.createConnection({
            host: process.env.DB_HOST,
            port: process.env.DB_PORT,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME,
            multipleStatements: true,
            ssl: {
                rejectUnauthorized: false
            }
        });

        console.log("Connected to Aiven MySQL.");
        console.log("Importing CreatorLog database...");

        await connection.query(sql);

        console.log("=================================");
        console.log("DATABASE IMPORT SUCCESSFUL!");
        console.log("=================================");

    } catch (error) {
        console.error("DATABASE IMPORT FAILED:");
        console.error(error.message);

    } finally {
        if (connection) {
            await connection.end();
        }
    }
}

importBackup();