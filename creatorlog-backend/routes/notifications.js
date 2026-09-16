const express = require("express");
const db = require("../db");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post("/", authMiddleware, async (req, res) => {
    try {
        const {
            project_id,
            title,
            message,
            type
        } = req.body;

        if (!title || !message) {
            return res.status(400).json({
                success: false,
                message: "Title and message are required."
            });
        }

        if (project_id) {
            const [projectRows] = await db.execute(
                `SELECT id
                 FROM projects
                 WHERE id = ?
                 AND account_id = ?`,
                [project_id, req.user.id]
            );

            if (projectRows.length === 0) {
                return res.status(404).json({
                    success: false,
                    message: "Project not found."
                });
            }
        }

        const [result] = await db.execute(
            `INSERT INTO notifications
            (account_id, project_id, title, message, type)
            VALUES (?, ?, ?, ?, ?)`,
            [
                req.user.id,
                project_id || null,
                title,
                message,
                type || "General"
            ]
        );

        res.status(201).json({
            success: true,
            message: "Notification created successfully.",
            notification: {
                id: result.insertId,
                project_id: project_id || null,
                title,
                message,
                type: type || "General",
                is_read: false
            }
        });

    } catch (error) {
        console.error("Create notification error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while creating notification."
        });
    }
});

router.get("/", authMiddleware, async (req, res) => {
    try {
        const [notifications] = await db.execute(
            `SELECT
                id,
                project_id,
                title,
                message,
                type,
                is_read,
                created_at
             FROM notifications
             WHERE account_id = ?
             ORDER BY created_at DESC`,
            [req.user.id]
        );

        res.json({
            success: true,
            notifications
        });
    } catch (error) {
        console.error("Get notifications error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while fetching notifications."
        });
    }
});

router.put("/:id/read", authMiddleware, async (req, res) => {
    try {
        const notificationId = req.params.id;

        const [result] = await db.execute(
            `UPDATE notifications
             SET is_read = TRUE
             WHERE id = ?
             AND account_id = ?`,
            [notificationId, req.user.id]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Notification not found."
            });
        }

        res.json({
            success: true,
            message: "Notification marked as read."
        });
    } catch (error) {
        console.error("Mark notification as read error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating notification."
        });
    }
});

router.delete("/:id", authMiddleware, async (req, res) => {
    try {
        const notificationId = req.params.id;

        const [result] = await db.execute(
            `DELETE FROM notifications
             WHERE id = ?
             AND account_id = ?`,
            [notificationId, req.user.id]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Notification not found."
            });
        }

        res.json({
            success: true,
            message: "Notification deleted successfully."
        });
    } catch (error) {
        console.error("Delete notification error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while deleting notification."
        });
    }
});

module.exports = router;