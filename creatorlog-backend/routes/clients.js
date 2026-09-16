const express = require("express");
const db = require("../db");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post("/", authMiddleware, async (req, res) => {
    try {
        const {
            full_name,
            email,
            phone,
            address,
            notes
        } = req.body;

        if (!full_name) {
            return res.status(400).json({
                success: false,
                message: "Full name is required."
            });
        }

        const [result] = await db.execute(
            `INSERT INTO clients
            (account_id, full_name, email, phone, address, notes)
            VALUES (?, ?, ?, ?, ?, ?)`,
            [
                req.user.id,
                full_name,
                email || null,
                phone || null,
                address || null,
                notes || null
            ]
        );

        res.status(201).json({
            success: true,
            message: "Client created successfully.",
            client: {
                id: result.insertId,
                full_name,
                email: email || null,
                phone: phone || null,
                address: address || null,
                notes: notes || null
            }
        });

    } catch (error) {
        console.error("Create client error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while creating client."
        });
    }
});

router.get("/", authMiddleware, async (req, res) => {
    try {
        const [clients] = await db.execute(
            `SELECT id, full_name, email, phone, address, notes, created_at
             FROM clients
             WHERE account_id = ?
             ORDER BY created_at DESC`,
            [req.user.id]
        );

        res.json({
            success: true,
            clients: clients
        });

    } catch (error) {
        console.error("Get clients error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while retrieving clients."
        });
    }
});

router.put("/:id", authMiddleware, async (req, res) => {
    try {
        const { id } = req.params;
        const {
            full_name,
            email,
            phone,
            address,
            notes
        } = req.body;

        if (!full_name) {
            return res.status(400).json({
                success: false,
                message: "Full name is required."
            });
        }

        const [result] = await db.execute(
            `UPDATE clients
             SET full_name = ?,
                 email = ?,
                 phone = ?,
                 address = ?,
                 notes = ?
             WHERE id = ?
             AND account_id = ?`,
            [
                full_name,
                email || null,
                phone || null,
                address || null,
                notes || null,
                id,
                req.user.id
            ]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Client not found."
            });
        }

        res.json({
            success: true,
            message: "Client updated successfully."
        });

    } catch (error) {
        console.error("Update client error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating client."
        });
    }
});

router.delete("/:id", authMiddleware, async (req, res) => {
    try {
        const { id } = req.params;

        const [result] = await db.execute(
            `DELETE FROM clients
             WHERE id = ?
             AND account_id = ?`,
            [id, req.user.id]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Client not found."
            });
        }

        res.json({
            success: true,
            message: "Client deleted successfully."
        });

    } catch (error) {
        console.error("Delete client error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while deleting client."
        });
    }
});

module.exports = router;