const express = require("express");
const db = require("../db");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post("/", authMiddleware, async (req, res) => {
    try {
        const {
            client_id,
            title,
            project_type,
            event_date,
            event_location,
            deadline,
            status
        } = req.body;

        if (!client_id || !title) {
            return res.status(400).json({
                success: false,
                message: "Client ID and project title are required."
            });
        }

        const [clientRows] = await db.execute(
            `SELECT id
             FROM clients
             WHERE id = ?
             AND account_id = ?`,
            [client_id, req.user.id]
        );

        if (clientRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Client not found."
            });
        }

        const [result] = await db.execute(
            `INSERT INTO projects
            (account_id, client_id, title, project_type, event_date,
             event_location, deadline, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
            [
                req.user.id,
                client_id,
                title,
                project_type || null,
                event_date || null,
                event_location || null,
                deadline || null,
                status || "Booked"
            ]
        );

        res.status(201).json({
            success: true,
            message: "Project created successfully.",
            project: {
                id: result.insertId,
                client_id,
                title,
                project_type: project_type || null,
                event_date: event_date || null,
                event_location: event_location || null,
                deadline: deadline || null,
                status: status || "Booked"
            }
        });

    } catch (error) {
        console.error("Create project error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while creating project."
        });
    }
});

router.get("/", authMiddleware, async (req, res) => {
    try {
        const [projects] = await db.execute(
            `SELECT
                p.id,
                p.client_id,
                c.full_name AS client_name,
                p.title,
                p.project_type,
                p.event_date,
                p.event_location,
                p.deadline,
                p.status,
                p.created_at
             FROM projects p
             INNER JOIN clients c ON p.client_id = c.id
             WHERE p.account_id = ?
             ORDER BY p.created_at DESC`,
            [req.user.id]
        );

        res.json({
            success: true,
            projects: projects
        });

    } catch (error) {
        console.error("Get projects error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while retrieving projects."
        });
    }
});

router.get("/:id", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;

        const [projects] = await db.execute(
            `SELECT
                p.id,
                p.client_id,
                c.full_name AS client_name,
                p.title,
                p.project_type,
                p.event_date,
                p.event_location,
                p.deadline,
                p.status,
                p.created_at
             FROM projects p
             INNER JOIN clients c ON p.client_id = c.id
             WHERE p.id = ?
             AND p.account_id = ?`,
            [projectId, req.user.id]
        );

        if (projects.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        res.json({
            success: true,
            project: projects[0]
        });

    } catch (error) {
        console.error("Get single project error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while retrieving project."
        });
    }
});

router.put("/:id", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;

        const {
            client_id,
            title,
            project_type,
            event_date,
            event_location,
            deadline,
            status
        } = req.body;

        if (!client_id || !title) {
            return res.status(400).json({
                success: false,
                message: "Client ID and project title are required."
            });
        }

        const [clientRows] = await db.execute(
            `SELECT id
             FROM clients
             WHERE id = ?
             AND account_id = ?`,
            [client_id, req.user.id]
        );

        if (clientRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Client not found."
            });
        }

        const [result] = await db.execute(
            `UPDATE projects
             SET client_id = ?,
                 title = ?,
                 project_type = ?,
                 event_date = ?,
                 event_location = ?,
                 deadline = ?,
                 status = ?
             WHERE id = ?
             AND account_id = ?`,
            [
                client_id,
                title,
                project_type || null,
                event_date || null,
                event_location || null,
                deadline || null,
                status || "Booked",
                projectId,
                req.user.id
            ]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        res.json({
            success: true,
            message: "Project updated successfully."
        });

    } catch (error) {
        console.error("Update project error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating project."
        });
    }
});

router.delete("/:id", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;

        const [result] = await db.execute(
            `DELETE FROM projects
             WHERE id = ?
             AND account_id = ?`,
            [projectId, req.user.id]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        res.json({
            success: true,
            message: "Project deleted successfully."
        });

    } catch (error) {
        console.error("Delete project error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while deleting project."
        });
    }
});

router.put("/:id/status", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;
        const { status } = req.body;

        const allowedStatuses = [
            "Booked",
            "Upcoming",
            "Shooting",
            "Editing",
            "Review",
            "Delivered"
        ];

        if (!status || !allowedStatuses.includes(status)) {
            return res.status(400).json({
                success: false,
                message: "Invalid project status."
            });
        }

        const [result] = await db.execute(
            `UPDATE projects
             SET status = ?
             WHERE id = ?
             AND account_id = ?`,
            [
                status,
                projectId,
                req.user.id
            ]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        res.json({
            success: true,
            message: "Project status updated successfully.",
            status: status
        });

    } catch (error) {
        console.error("Update project status error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating project status."
        });
    }
});

router.post("/:id/payments", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;
        const {
            amount,
            payment_method,
            notes
        } = req.body;

        if (!amount || Number(amount) <= 0) {
            return res.status(400).json({
                success: false,
                message: "Payment amount must be greater than 0."
            });
        }

        const [projectRows] = await db.execute(
            `SELECT id
             FROM projects
             WHERE id = ?
             AND account_id = ?`,
            [projectId, req.user.id]
        );

        if (projectRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        const [result] = await db.execute(
            `INSERT INTO payments
             (project_id, amount, payment_method, notes)
             VALUES (?, ?, ?, ?)`,
            [
                projectId,
                amount,
                payment_method || null,
                notes || null
            ]
        );

        res.status(201).json({
            success: true,
            message: "Payment recorded successfully.",
            payment: {
                id: result.insertId,
                project_id: Number(projectId),
                amount: Number(amount),
                payment_method: payment_method || null,
                notes: notes || null
            }
        });

    } catch (error) {
        console.error("Create payment error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while recording payment."
        });
    }
});

router.get("/:id/payments", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;

        const [payments] = await db.execute(
            `SELECT
                py.id,
                py.project_id,
                py.amount,
                py.payment_method,
                py.notes,
                py.paid_at
             FROM payments py
             INNER JOIN projects p
                 ON py.project_id = p.id
             WHERE py.project_id = ?
             AND p.account_id = ?
             ORDER BY py.paid_at DESC`,
            [projectId, req.user.id]
        );

        res.json({
            success: true,
            payments: payments
        });

    } catch (error) {
        console.error("Get payments error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while retrieving payments."
        });
    }
});

router.get("/:id/balance", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;

        const [projectRows] = await db.execute(
            `SELECT
                p.id,
                p.title,
                pf.total_fee
             FROM projects p
             LEFT JOIN project_finance pf
                 ON p.id = pf.project_id
             WHERE p.id = ?
             AND p.account_id = ?`,
            [projectId, req.user.id]
        );

        if (projectRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        const totalFee = Number(projectRows[0].total_fee || 0);

        const [paymentRows] = await db.execute(
            `SELECT COALESCE(SUM(amount), 0) AS total_paid
             FROM payments
             WHERE project_id = ?`,
            [projectId]
        );

        const totalPaid = Number(paymentRows[0].total_paid || 0);
        const remainingBalance = totalFee - totalPaid;

        res.json({
            success: true,
            project: {
                id: projectRows[0].id,
                title: projectRows[0].title
            },
            finance: {
                total_fee: totalFee,
                total_paid: totalPaid,
                remaining_balance: remainingBalance
            }
        });

    } catch (error) {
        console.error("Get project balance error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while calculating project balance."
        });
    }
});

router.put("/:id/finance", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;
        const { total_fee } = req.body;

        if (total_fee === undefined || Number(total_fee) < 0) {
            return res.status(400).json({
                success: false,
                message: "Total fee must be 0 or greater."
            });
        }

        const [projectRows] = await db.execute(
            `SELECT id
             FROM projects
             WHERE id = ?
             AND account_id = ?`,
            [projectId, req.user.id]
        );

        if (projectRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        await db.execute(
            `INSERT INTO project_finance
             (project_id, total_fee)
             VALUES (?, ?)
             ON DUPLICATE KEY UPDATE
             total_fee = VALUES(total_fee)`,
            [projectId, total_fee]
        );

        res.json({
            success: true,
            message: "Project fee updated successfully.",
            total_fee: Number(total_fee)
        });

    } catch (error) {
        console.error("Update project finance error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating project fee."
        });
    }
});

router.post("/:id/tasks", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;
        const {
            title,
            category,
            status,
            due_date,
            notes
        } = req.body;

        if (!title) {
            return res.status(400).json({
                success: false,
                message: "Task title is required."
            });
        }

        const [projectRows] = await db.execute(
            `SELECT id
             FROM projects
             WHERE id = ?
             AND account_id = ?`,
            [projectId, req.user.id]
        );

        if (projectRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        const [result] = await db.execute(
            `INSERT INTO tasks
            (project_id, title, category, status, due_date, notes)
            VALUES (?, ?, ?, ?, ?, ?)`,
            [
                projectId,
                title,
                category || null,
                status || "Pending",
                due_date || null,
                notes || null
            ]
        );

        res.status(201).json({
            success: true,
            message: "Task created successfully.",
            task: {
                id: result.insertId,
                project_id: Number(projectId),
                title,
                category: category || null,
                status: status || "Pending",
                due_date: due_date || null,
                notes: notes || null
            }
        });

    } catch (error) {
        console.error("Create task error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while creating task."
        });
    }
});

router.get("/:id/tasks", authMiddleware, async (req, res) => {
    try {
        const projectId = req.params.id;

        const [projectRows] = await db.execute(
            `SELECT id
             FROM projects
             WHERE id = ?
             AND account_id = ?`,
            [projectId, req.user.id]
        );

        if (projectRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        const [taskRows] = await db.execute(
            `SELECT *
             FROM tasks
             WHERE project_id = ?
             ORDER BY created_at DESC`,
            [projectId]
        );

        res.json({
            success: true,
            tasks: taskRows
        });

    } catch (error) {
        console.error("Get project tasks error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while getting project tasks."
        });
    }
});

router.get("/dashboard/statistics", authMiddleware, async (req, res) => {
    try {
        const [rows] = await db.execute(
            `SELECT
                COUNT(*) AS total_projects,
                SUM(CASE WHEN status = 'Booked' THEN 1 ELSE 0 END) AS booked,
                SUM(CASE WHEN status = 'Upcoming' THEN 1 ELSE 0 END) AS upcoming,
                SUM(CASE WHEN status = 'Shooting' THEN 1 ELSE 0 END) AS shooting,
                SUM(CASE WHEN status = 'Editing' THEN 1 ELSE 0 END) AS editing,
                SUM(CASE WHEN status = 'Review' THEN 1 ELSE 0 END) AS review,
                SUM(CASE WHEN status = 'Delivered' THEN 1 ELSE 0 END) AS delivered
             FROM projects
             WHERE account_id = ?`,
            [req.user.id]
        );

        const statistics = rows[0];

        res.json({
            success: true,
            statistics: {
                total_projects: statistics.total_projects || 0,
                booked: statistics.booked || 0,
                upcoming: statistics.upcoming || 0,
                shooting: statistics.shooting || 0,
                editing: statistics.editing || 0,
                review: statistics.review || 0,
                delivered: statistics.delivered || 0
            }
        });
    } catch (error) {
        console.error("Project statistics error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while fetching project statistics."
        });
    }
});

router.get("/dashboard/upcoming-deadlines", authMiddleware, async (req, res) => {
    try {
        const [projects] = await db.execute(
            `SELECT
                p.id,
                p.title,
                p.project_type,
                p.event_date,
                p.event_location,
                p.deadline,
                p.status,
                c.full_name AS client_name
             FROM projects p
             INNER JOIN clients c
                 ON p.client_id = c.id
             WHERE p.account_id = ?
             AND p.deadline IS NOT NULL
             AND p.deadline >= CURDATE()
             ORDER BY p.deadline ASC`,
            [req.user.id]
        );

        res.json({
            success: true,
            projects
        });
    } catch (error) {
        console.error("Upcoming deadlines error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while fetching upcoming deadlines."
        });
    }
});

router.get("/dashboard/history", authMiddleware, async (req, res) => {
    try {
        const [projects] = await db.execute(
            `SELECT
                p.id,
                p.title,
                p.project_type,
                p.event_date,
                p.event_location,
                p.deadline,
                p.status,
                c.full_name AS client_name,
                p.created_at
             FROM projects p
             INNER JOIN clients c
                 ON p.client_id = c.id
             WHERE p.account_id = ?
             ORDER BY p.created_at DESC`,
            [req.user.id]
        );

        res.json({
            success: true,
            projects
        });
    } catch (error) {
        console.error("Project history error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while fetching project history."
        });
    }
});

router.put("/:projectId/tasks/:taskId", authMiddleware, async (req, res) => {
    try {
        const { projectId, taskId } = req.params;

        const {
            title,
            category,
            status,
            due_date,
            notes
        } = req.body;

        const [projectRows] = await db.execute(
            `SELECT id
             FROM projects
             WHERE id = ?
             AND account_id = ?`,
            [projectId, req.user.id]
        );

        if (projectRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        const [taskRows] = await db.execute(
            `SELECT id
             FROM tasks
             WHERE id = ?
             AND project_id = ?`,
            [taskId, projectId]
        );

        if (taskRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Task not found."
            });
        }

        if (!title) {
            return res.status(400).json({
                success: false,
                message: "Task title is required."
            });
        }

        await db.execute(
            `UPDATE tasks
             SET title = ?,
                 category = ?,
                 status = ?,
                 due_date = ?,
                 notes = ?
             WHERE id = ?
             AND project_id = ?`,
            [
                title,
                category || null,
                status || "Pending",
                due_date || null,
                notes || null,
                taskId,
                projectId
            ]
        );

        res.json({
            success: true,
            message: "Task updated successfully."
        });

    } catch (error) {
        console.error("Update task error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating task."
        });
    }
});

router.put("/:projectId/tasks/:taskId/status", authMiddleware, async (req, res) => {
    try {
        const { projectId, taskId } = req.params;
        const { status } = req.body;

        const allowedStatuses = [
            "Pending",
            "In Progress",
            "Completed"
        ];

        if (!status || !allowedStatuses.includes(status)) {
            return res.status(400).json({
                success: false,
                message: "Invalid task status."
            });
        }

        const [projectRows] = await db.execute(
            `SELECT id
             FROM projects
             WHERE id = ?
             AND account_id = ?`,
            [projectId, req.user.id]
        );

        if (projectRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        const [taskRows] = await db.execute(
            `SELECT id
             FROM tasks
             WHERE id = ?
             AND project_id = ?`,
            [taskId, projectId]
        );

        if (taskRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Task not found."
            });
        }

        await db.execute(
            `UPDATE tasks
             SET status = ?
             WHERE id = ?
             AND project_id = ?`,
            [status, taskId, projectId]
        );

        res.json({
            success: true,
            message: "Task status updated successfully.",
            status: status
        });

    } catch (error) {
        console.error("Update task status error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while updating task status."
        });
    }
});

router.delete("/:projectId/tasks/:taskId", authMiddleware, async (req, res) => {
    try {
        const { projectId, taskId } = req.params;

        const [projectRows] = await db.execute(
            `SELECT id
             FROM projects
             WHERE id = ?
             AND account_id = ?`,
            [projectId, req.user.id]
        );

        if (projectRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Project not found."
            });
        }

        const [taskRows] = await db.execute(
            `SELECT id
             FROM tasks
             WHERE id = ?
             AND project_id = ?`,
            [taskId, projectId]
        );

        if (taskRows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Task not found."
            });
        }

        await db.execute(
            `DELETE FROM tasks
             WHERE id = ?
             AND project_id = ?`,
            [taskId, projectId]
        );

        res.json({
            success: true,
            message: "Task deleted successfully."
        });

    } catch (error) {
        console.error("Delete task error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while deleting task."
        });
    }
});

router.get("/dashboard/summary", authMiddleware, async (req, res) => {
    try {
        const accountId = req.user.id;

        const [clientRows] = await db.execute(
            `SELECT COUNT(*) AS total_clients
             FROM clients
             WHERE account_id = ?`,
            [accountId]
        );

        const [projectRows] = await db.execute(
            `SELECT
                COUNT(*) AS total_projects,
                SUM(CASE WHEN status != 'Delivered' THEN 1 ELSE 0 END) AS active_projects,
                SUM(CASE WHEN status = 'Delivered' THEN 1 ELSE 0 END) AS completed_projects
             FROM projects
             WHERE account_id = ?`,
            [accountId]
        );

        const [financeRows] = await db.execute(
            `SELECT
                COALESCE(SUM(pf.total_fee), 0) AS total_revenue,
                COALESCE((
                    SELECT SUM(py.amount)
                    FROM payments py
                    INNER JOIN projects p2
                        ON py.project_id = p2.id
                    WHERE p2.account_id = ?
                ), 0) AS total_paid
             FROM project_finance pf
             INNER JOIN projects p
                 ON pf.project_id = p.id
             WHERE p.account_id = ?`,
            [accountId, accountId]
        );

        const totalRevenue = Number(financeRows[0].total_revenue);
        const totalPaid = Number(financeRows[0].total_paid);
        const totalBalance = totalRevenue - totalPaid;

        res.json({
            success: true,
            summary: {
                total_clients: clientRows[0].total_clients,
                total_projects: projectRows[0].total_projects,
                active_projects: projectRows[0].active_projects || 0,
                completed_projects: projectRows[0].completed_projects || 0,
                total_revenue: totalRevenue,
                total_paid: totalPaid,
                total_balance: totalBalance
            }
        });
    } catch (error) {
        console.error("Dashboard summary error:", error);

        res.status(500).json({
            success: false,
            message: "Server error while fetching dashboard summary."
        });
    }
});

module.exports = router;