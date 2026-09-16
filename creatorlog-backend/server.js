const express = require("express");
const cors = require("cors");
const db = require("./db");
const authRoutes = require("./routes/auth");
const authMiddleware = require("./middleware/authMiddleware");
const clientRoutes = require("./routes/clients");
const projectRoutes = require("./routes/projects");
const notificationRoutes = require("./routes/notifications");
require("dotenv").config();

const app = express();

app.use(cors());
app.use(express.json());

app.use("/api/auth", authRoutes);
app.use("/api/clients", clientRoutes);
app.use("/api/projects", projectRoutes);
app.use("/api/notifications", notificationRoutes);

db.getConnection()
    .then(connection => {
        console.log("MySQL database connected successfully!");
        connection.release();
    })
    .catch(error => {
        console.error("MySQL connection failed:", error.message);
    });

app.get("/api/health", (req, res) => {
    res.json({
        success: true,
        message: "CreatorLog Backend is running!"
    });
});

app.get("/api/protected", authMiddleware, (req, res) => {
    res.json({
        success: true,
        message: "You accessed a protected route!",
        user: req.user
    });
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, "0.0.0.0", () => {
    console.log(`CreatorLog Backend running on port ${PORT}`);
});