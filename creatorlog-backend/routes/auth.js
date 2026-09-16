const express = require("express");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const db = require("../db");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post("/register", async (req, res) => {
    try {
        const { full_name, email, password } = req.body;

        if (!full_name || !email || !password) {
            return res.status(400).json({
                success: false,
                message: "Full name, email, and password are required."
            });
        }

        const cleanName = full_name.trim();
        const cleanEmail = email.trim().toLowerCase();

        if (password.length < 6) {
            return res.status(400).json({
                success: false,
                message: "Password must be at least 6 characters."
            });
        }

        const [existing] = await db.execute(
            "SELECT id FROM accounts WHERE email = ?",
            [cleanEmail]
        );

        if (existing.length > 0) {
            return res.status(409).json({
                success: false,
                message: "An account with this email already exists."
            });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const [result] = await db.execute(
            `INSERT INTO accounts (full_name, email, password)
             VALUES (?, ?, ?)`,
            [cleanName, cleanEmail, hashedPassword]
        );

        res.status(201).json({
            success: true,
            message: "Registration successful.",
            user: {
                id: result.insertId,
                full_name: cleanName,
                email: cleanEmail
            }
        });
    } catch (error) {
        console.error("Register error:", error);

        res.status(500).json({
            success: false,
            message: "Server error during registration."
        });
    }
});

router.post("/login", async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: "Email and password are required."
            });
        }

        const cleanEmail = email.trim().toLowerCase();

        const [users] = await db.execute(
            `SELECT id, full_name, email, password
             FROM accounts
             WHERE email = ?`,
            [cleanEmail]
        );

        if (users.length === 0) {
            return res.status(401).json({
                success: false,
                message: "Invalid email or password."
            });
        }

        const user = users[0];

        const passwordMatches = await bcrypt.compare(
            password,
            user.password
        );

        if (!passwordMatches) {
            return res.status(401).json({
                success: false,
                message: "Invalid email or password."
            });
        }

        const token = jwt.sign(
            {
                id: user.id,
                email: user.email
            },
            process.env.JWT_SECRET,
            {
                expiresIn: "7d"
            }
        );

        res.json({
            success: true,
            message: "Login successful.",
            token: token,
            user: {
                id: user.id,
                full_name: user.full_name,
                email: user.email
            }
        });
    } catch (error) {
        console.error("Login error:", error);

        res.status(500).json({
            success: false,
            message: "Server error during login."
        });
    }
});

router.get("/profile", authMiddleware, async (req, res) => {
    try {
        const [users] = await db.execute(
            `SELECT id, full_name, email
             FROM accounts
             WHERE id = ?`,
            [req.user.id]
        );

        if (users.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Account not found."
            });
        }

        res.json({
            success: true,
            user: users[0]
        });
    } catch (error) {
        console.error("Get profile error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while retrieving profile."
        });
    }
});

router.put("/profile", authMiddleware, async (req, res) => {
    try {
        const { full_name, email } = req.body;

        if (!full_name || !full_name.trim()) {
            return res.status(400).json({
                success: false,
                message: "Full name is required."
            });
        }

        if (!email || !email.trim()) {
            return res.status(400).json({
                success: false,
                message: "Email is required."
            });
        }

        const cleanName = full_name.trim();
        const cleanEmail = email.trim().toLowerCase();

        const [existing] = await db.execute(
            `SELECT id
             FROM accounts
             WHERE email = ?
             AND id <> ?`,
            [cleanEmail, req.user.id]
        );

        if (existing.length > 0) {
            return res.status(409).json({
                success: false,
                message: "That email is already in use by another account."
            });
        }

        const [result] = await db.execute(
            `UPDATE accounts
             SET full_name = ?, email = ?
             WHERE id = ?`,
            [
                cleanName,
                cleanEmail,
                req.user.id
            ]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Account not found."
            });
        }

        const newToken = jwt.sign(
            {
                id: req.user.id,
                email: cleanEmail
            },
            process.env.JWT_SECRET,
            {
                expiresIn: "7d"
            }
        );

        res.json({
            success: true,
            message: "Profile updated successfully.",
            token: newToken,
            user: {
                id: req.user.id,
                full_name: cleanName,
                email: cleanEmail
            }
        });
    } catch (error) {
        console.error("Update profile error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating profile."
        });
    }
});

router.get("/profile", authMiddleware, async (req, res) => {
    try {
        const [users] = await db.execute(
            `SELECT id, full_name, email
             FROM accounts
             WHERE id = ?`,
            [req.user.id]
        );

        if (users.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Account not found."
            });
        }

        res.json({
            success: true,
            user: users[0]
        });

    } catch (error) {
        console.error("Get profile error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while retrieving profile."
        });
    }
});


router.put("/profile", authMiddleware, async (req, res) => {
    try {
        const { full_name, email } = req.body;

        if (!full_name || !full_name.trim()) {
            return res.status(400).json({
                success: false,
                message: "Full name is required."
            });
        }

        if (!email || !email.trim()) {
            return res.status(400).json({
                success: false,
                message: "Email is required."
            });
        }

        const cleanName = full_name.trim();
        const cleanEmail = email.trim().toLowerCase();

        const [existing] = await db.execute(
            `SELECT id
             FROM accounts
             WHERE email = ?
             AND id <> ?`,
            [cleanEmail, req.user.id]
        );

        if (existing.length > 0) {
            return res.status(409).json({
                success: false,
                message: "That email is already in use by another account."
            });
        }

        const [result] = await db.execute(
            `UPDATE accounts
             SET full_name = ?, email = ?
             WHERE id = ?`,
            [
                cleanName,
                cleanEmail,
                req.user.id
            ]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Account not found."
            });
        }

        const newToken = jwt.sign(
            {
                id: req.user.id,
                email: cleanEmail
            },
            process.env.JWT_SECRET,
            {
                expiresIn: "7d"
            }
        );

        res.json({
            success: true,
            message: "Profile updated successfully.",
            token: newToken,
            user: {
                id: req.user.id,
                full_name: cleanName,
                email: cleanEmail
            }
        });

    } catch (error) {
        console.error("Update profile error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating profile."
        });
    }
});

module.exports = router;