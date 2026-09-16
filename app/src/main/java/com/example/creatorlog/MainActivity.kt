package com.example.creatorlog

import android.os.Bundle
import android.content.Context
import android.app.DatePickerDialog
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

data class Client(
    val id: Int,
    val fullName: String,
    val contactNumber: String,
    val email: String,
    val notes: String
)

data class Project(
    val id: Int,
    val clientId: Int,
    val projectName: String,
    val projectType: String,
    val eventDate: String,
    val location: String,
    val deadline: String,
    val status: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }

    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("CreatorLogFCM", "FCM Token: ${task.result}")
            } else {
                Log.e("CreatorLogFCM", "FCM Token failed", task.exception)
            }
        }

    setContent {
        CreatorLogApp()
    }
    }
}

@Composable
fun CreatorLogApp() {

    val context = LocalContext.current
    val database = remember {
        CreatorLogDatabase.getDatabase(context)
    }
val scope = rememberCoroutineScope()

var currentScreen by remember { mutableStateOf("login") }
var isAuthenticated by remember { mutableStateOf(false) }
var isCheckingSession by remember { mutableStateOf(true) }

var showLoginSuccessDialog by remember { mutableStateOf(false) }
var showRegistrationSuccessDialog by remember { mutableStateOf(false) }
var showAuthErrorDialog by remember { mutableStateOf(false) }
var authErrorMessage by remember { mutableStateOf("") }
var showLogoutDialog by remember { mutableStateOf(false) }
var clients by remember { mutableStateOf<List<Client>>(emptyList()) }
var showClientAddedDialog by remember { mutableStateOf(false) }
    var showClientErrorDialog by remember { mutableStateOf(false) }
    var clientErrorMessage by remember { mutableStateOf("") }
var showClientUpdatedDialog by remember { mutableStateOf(false) }
var editingClientId by remember { mutableStateOf<Int?>(null) }
var viewingClientId by remember { mutableStateOf<Int?>(null) }
var deletingClientId by remember { mutableStateOf<Int?>(null) }
var showClientDeletedDialog by remember { mutableStateOf(false) }
var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
var selectedProjectId by remember { mutableStateOf<Int?>(null) }
var showProjectAddedDialog by remember { mutableStateOf(false) }
var showProjectUpdatedDialog by remember { mutableStateOf(false) }
var showProjectStatusUpdatedDialog by remember { mutableStateOf(false) }
var showProjectErrorDialog by remember { mutableStateOf(false) }
var projectErrorMessage by remember { mutableStateOf("") }
var schedulingSelectedDate by remember { mutableStateOf<String?>(null) }
var reportProjectId by remember { mutableStateOf<Int?>(null) }
var profile by remember { mutableStateOf<ProfileUser?>(null) }
var showProfileUpdatedDialog by remember { mutableStateOf(false) }
var showProfileErrorDialog by remember { mutableStateOf(false) }
var profileErrorMessage by remember { mutableStateOf("") }

LaunchedEffect(Unit) {
    val savedToken = TokenManager.getToken(context)

    if (!savedToken.isNullOrBlank()) {
        try {
            val protectedResponse = AuthApi.getProtected(savedToken)

            if (protectedResponse.success) {
                isAuthenticated = true
                currentScreen = "home"
            } else {
            TokenManager.clearToken(context)
        }
} catch (e: Exception) {
TokenManager.clearToken(context)
}
}

isCheckingSession = false
}

LaunchedEffect(isAuthenticated) {
    if (!isAuthenticated) return@LaunchedEffect

    val savedToken = TokenManager.getToken(context)

    if (!savedToken.isNullOrBlank()) {
        try {
            val response = ClientApi.getClients(savedToken)

            if (response.success) {
                clients = response.clients?.map { apiClient ->
                    Client(
                        id = apiClient.id,
                        fullName = apiClient.full_name,
                        contactNumber = apiClient.phone ?: "",
                        email = apiClient.email ?: "",
                        notes = apiClient.notes ?: ""
                    )
                } ?: emptyList()
            } else {
                println("CLIENT API LOAD FAILED: ${response.message}")
            }
        } catch (e: Exception) {
            println("CLIENT API LOAD ERROR: ${e.message}")
        }
    }
}

LaunchedEffect(isAuthenticated) {
    if (!isAuthenticated) return@LaunchedEffect

    val savedToken = TokenManager.getToken(context)
    if (!savedToken.isNullOrBlank()) {
        try {
            val response = ProjectApi.getProjects(savedToken)
            if (response.success) {
                projects = response.projects?.map { apiProject ->
                    Project(
                        id = apiProject.id,
                        clientId = apiProject.client_id,
                        projectName = apiProject.title,
                        projectType = apiProject.project_type ?: "",
                        eventDate = ProjectApi.displayDate(apiProject.event_date),
                        location = apiProject.event_location ?: "",
                        deadline = ProjectApi.displayDate(apiProject.deadline),
                        status = apiProject.status ?: "Booked"
                    )
                } ?: emptyList()
            }
        } catch (e: Exception) {
            println("PROJECT API LOAD ERROR: ${e.message}")
        }
    }
}

LaunchedEffect(isAuthenticated) {
    if (!isAuthenticated) return@LaunchedEffect

    val savedToken = TokenManager.getToken(context)
    if (!savedToken.isNullOrBlank()) {
        try {
            val response = ProfileApi.getProfile(savedToken)
            if (response.success) {
                profile = response.user
            } else {
                println("PROFILE API LOAD FAILED: ${response.message}")
            }
        } catch (e: Exception) {
            println("PROFILE API LOAD ERROR: ${e.message}")
        }
    }
}

MaterialTheme {
    Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
) {

    when {
        isCheckingSession -> {
            Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
            text = "Checking session...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

isAuthenticated && currentScreen == "add_client" -> {
    AddClientScreen(
    onBackToClients = {
        currentScreen = "clients"
    },
onClientSaved = { fullName, contactNumber, email, notes ->
    scope.launch {
        try {
            val savedToken = TokenManager.getToken(context)

            if (savedToken.isNullOrBlank()) {
                clientErrorMessage = "Your session has expired. Please login again."
                showClientErrorDialog = true
                return@launch
            }

            val response = ClientApi.createClient(
                token = savedToken,
                fullName = fullName,
                email = email.ifBlank { null },
                phone = contactNumber,
                address = null,
                notes = notes.ifBlank { null }
            )

            if (response.success) {
                val refreshedResponse = ClientApi.getClients(savedToken)

                if (refreshedResponse.success) {
                    clients = refreshedResponse.clients?.map { apiClient ->
                        Client(
                            id = apiClient.id,
                            fullName = apiClient.full_name,
                            contactNumber = apiClient.phone ?: "",
                            email = apiClient.email ?: "",
                            notes = apiClient.notes ?: ""
                        )
                    } ?: emptyList()
                }

                showClientAddedDialog = true
                currentScreen = "clients"
            } else {
                clientErrorMessage = response.message
                showClientErrorDialog = true
            }
        } catch (e: Exception) {
            println("CREATE CLIENT API ERROR: ${e.message}")
            clientErrorMessage = "Unable to add client. Please check your connection and try again."
            showClientErrorDialog = true
        }
    }
}
)
}

isAuthenticated && currentScreen == "view_client" -> {
    val clientToView = clients.find { it.id == viewingClientId }

    if (clientToView != null) {
        ViewClientScreen(
        client = clientToView,
        onBackToClients = {
            currentScreen = "clients"
            viewingClientId = null
        },
    onEditClient = {
        editingClientId = clientToView.id
        viewingClientId = null
        currentScreen = "edit_client"
    },
onDeleteClient = {
    deletingClientId = clientToView.id
    viewingClientId = null
}
)
} else {
currentScreen = "clients"
viewingClientId = null
}
}

isAuthenticated && currentScreen == "edit_client" -> {
    val clientToEdit = clients.find { it.id == editingClientId }

    if (clientToEdit != null) {
        EditClientScreen(
        client = clientToEdit,
        onBackToClients = {
            currentScreen = "clients"
            editingClientId = null
        },
    onClientUpdated = { updatedClient ->
        scope.launch {
            try {
                val savedToken = TokenManager.getToken(context)

                if (savedToken.isNullOrBlank()) {
                    clientErrorMessage = "Your session has expired. Please login again."
                    showClientErrorDialog = true
                    return@launch
                }

                val response = ClientApi.updateClient(
                    token = savedToken,
                    clientId = updatedClient.id,
                    fullName = updatedClient.fullName,
                    email = updatedClient.email.ifBlank { null },
                    phone = updatedClient.contactNumber,
                    address = null,
                    notes = updatedClient.notes.ifBlank { null }
                )

                if (response.success) {
                    val refreshedResponse = ClientApi.getClients(savedToken)

                    if (refreshedResponse.success) {
                        clients = refreshedResponse.clients?.map { apiClient ->
                            Client(
                                id = apiClient.id,
                                fullName = apiClient.full_name,
                                contactNumber = apiClient.phone ?: "",
                                email = apiClient.email ?: "",
                                notes = apiClient.notes ?: ""
                            )
                        } ?: emptyList()
                    }

                    showClientUpdatedDialog = true
                } else {
                    clientErrorMessage = response.message
                    showClientErrorDialog = true
                }
            } catch (e: Exception) {
                println("UPDATE CLIENT API ERROR: ${e.message}")
                clientErrorMessage = "Unable to update client. Please check your connection and try again."
                showClientErrorDialog = true
            }
        }
    }
)
} else {
currentScreen = "clients"
}
}

isAuthenticated && currentScreen == "view_project" -> {
    val projectToView = projects.find { it.id == selectedProjectId }

    if (projectToView != null) {
        ProjectDetailsScreen(
        project = projectToView,
        clientName = clients
        .find { it.id == projectToView.clientId }
        ?.fullName
        ?: "Unknown client",
        onEditProject = {
            currentScreen = "edit_project"
        },
    onChangeStatus = {
        currentScreen = "status_project"
    },
onOpenChecklist = {
    currentScreen = "project_checklist"
},
onBackToProjects = {
    selectedProjectId = null
    currentScreen = "projects"
}
)
} else {
selectedProjectId = null
currentScreen = "projects"
}
}

isAuthenticated && currentScreen == "project_checklist" -> {
    val projectForTasks = projects.find { it.id == selectedProjectId }

    if (projectForTasks != null) {
        ProjectChecklistScreen(
        project = projectForTasks,
        onBackToProjectDetails = {
            currentScreen = "view_project"
        }
)
} else {
selectedProjectId = null
currentScreen = "projects"
}
}

isAuthenticated && currentScreen == "status_project" -> {
    val projectToUpdate = projects.find { it.id == selectedProjectId }

    if (projectToUpdate != null) {
        ProjectStatusScreen(
        project = projectToUpdate,
        onBackToProjectDetails = {
            currentScreen = "view_project"
        },
    onStatusUpdated = { newStatus ->
        scope.launch {
            try {
                val savedToken = TokenManager.getToken(context)
                if (savedToken.isNullOrBlank()) {
                    projectErrorMessage = "Your session has expired. Please login again."
                    showProjectErrorDialog = true
                    return@launch
                }

                val response = ProjectApi.updateProject(
                    token = savedToken,
                    projectId = projectToUpdate.id,
                    clientId = projectToUpdate.clientId,
                    title = projectToUpdate.projectName,
                    projectType = projectToUpdate.projectType,
                    eventDate = ProjectApi.apiDate(projectToUpdate.eventDate),
                    eventLocation = projectToUpdate.location,
                    deadline = ProjectApi.apiDate(projectToUpdate.deadline),
                    status = newStatus
                )

                if (response.success) {
                    val refreshed = ProjectApi.getProjects(savedToken)
                    if (refreshed.success) {
                        projects = refreshed.projects?.map { apiProject ->
                            Project(
                                id = apiProject.id,
                                clientId = apiProject.client_id,
                                projectName = apiProject.title,
                                projectType = apiProject.project_type ?: "",
                                eventDate = ProjectApi.displayDate(apiProject.event_date),
                                location = apiProject.event_location ?: "",
                                deadline = ProjectApi.displayDate(apiProject.deadline),
                                status = apiProject.status ?: "Booked"
                            )
                        } ?: emptyList()
                    }
                    showProjectStatusUpdatedDialog = true
                } else {
                    projectErrorMessage = response.message
                    showProjectErrorDialog = true
                }
            } catch (e: Exception) {
                println("UPDATE PROJECT STATUS API ERROR: ${e.message}")
                projectErrorMessage = "Unable to update project status. Please check your connection and try again."
                showProjectErrorDialog = true
            }
        }
    }
)
} else {
currentScreen = "projects"
}
}

isAuthenticated && currentScreen == "edit_project" -> {
    val projectToEdit = projects.find { it.id == selectedProjectId }

    if (projectToEdit != null) {
        EditProjectScreen(
        project = projectToEdit,
        clients = clients,
        onBackToProjectDetails = {
            currentScreen = "view_project"
        },
    onProjectUpdated = { updatedProject ->
        scope.launch {
            try {
                val savedToken = TokenManager.getToken(context)
                if (savedToken.isNullOrBlank()) {
                    projectErrorMessage = "Your session has expired. Please login again."
                    showProjectErrorDialog = true
                    return@launch
                }

                val response = ProjectApi.updateProject(
                    token = savedToken,
                    projectId = updatedProject.id,
                    clientId = updatedProject.clientId,
                    title = updatedProject.projectName,
                    projectType = updatedProject.projectType,
                    eventDate = ProjectApi.apiDate(updatedProject.eventDate),
                    eventLocation = updatedProject.location,
                    deadline = ProjectApi.apiDate(updatedProject.deadline),
                    status = updatedProject.status
                )

                if (response.success) {
                    val refreshed = ProjectApi.getProjects(savedToken)
                    if (refreshed.success) {
                        projects = refreshed.projects?.map { apiProject ->
                            Project(
                                id = apiProject.id,
                                clientId = apiProject.client_id,
                                projectName = apiProject.title,
                                projectType = apiProject.project_type ?: "",
                                eventDate = ProjectApi.displayDate(apiProject.event_date),
                                location = apiProject.event_location ?: "",
                                deadline = ProjectApi.displayDate(apiProject.deadline),
                                status = apiProject.status ?: "Booked"
                            )
                        } ?: emptyList()
                    }
                    showProjectUpdatedDialog = true
                } else {
                    projectErrorMessage = response.message
                    showProjectErrorDialog = true
                }
            } catch (e: Exception) {
                println("UPDATE PROJECT API ERROR: ${e.message}")
                projectErrorMessage = "Unable to update project. Please check your connection and try again."
                showProjectErrorDialog = true
            }
        }
    }
)
} else {
selectedProjectId = null
currentScreen = "projects"
}
}

isAuthenticated && currentScreen == "scheduling" -> {
    SchedulingScreen(
    projects = projects,
    selectedDate = schedulingSelectedDate,
    onSelectDate = { date -> schedulingSelectedDate = date },
    onUpcomingShoots = {
        currentScreen = "upcoming_shoots"
    },
onScheduledActivities = {
    currentScreen = "scheduled_activities"
},
onBackToDashboard = {
    schedulingSelectedDate = null
    currentScreen = "home"
}
)
}

isAuthenticated && currentScreen == "upcoming_shoots" -> {
    UpcomingShootsScreen(
    projects = projects,
    onProjectClick = { projectId ->
        selectedProjectId = projectId
        currentScreen = "view_project"
    },
onBackToScheduling = {
    currentScreen = "scheduling"
}
)
}

isAuthenticated && currentScreen == "scheduled_activities" -> {
    ScheduledActivitiesScreen(
    projects = projects,
    onProjectClick = { projectId ->
        selectedProjectId = projectId
        currentScreen = "view_project"
    },
onBackToScheduling = {
    currentScreen = "scheduling"
}
)
}

isAuthenticated && currentScreen == "deadline_reminders" -> {
    DeadlineRemindersScreen(
    projects = projects,
    onProjectClick = { projectId ->
        selectedProjectId = projectId
        currentScreen = "view_project"
    },
onBack = { currentScreen = "home" }
)
}

isAuthenticated && currentScreen == "shoot_reminders" -> {
    ShootRemindersScreen(
    projects = projects,
    onProjectClick = { projectId ->
        selectedProjectId = projectId
        currentScreen = "view_project"
    },
onBack = { currentScreen = "home" }
)
}

isAuthenticated && currentScreen == "pending_tasks" -> {
    PendingTaskRemindersScreen(
    projects = projects,
    onProjectClick = { projectId ->
        selectedProjectId = projectId
        currentScreen = "view_project"
    },
onBack = { currentScreen = "home" }
)
}

isAuthenticated && currentScreen == "project_history" -> {
    ProjectHistoryScreen(
    projects = projects,
    clients = clients,
    onProjectClick = { projectId ->
        selectedProjectId = projectId
        currentScreen = "view_project"
    },
onBack = { currentScreen = "home" }
)
}

isAuthenticated && currentScreen == "summary_dashboard" -> {
    ProjectSummaryDashboardScreen(
    projects = projects,
    clients = clients,
    onProjectClick = { projectId ->
        selectedProjectId = projectId
        currentScreen = "view_project"
    },
onBack = { currentScreen = "home" }
)
}

isAuthenticated && currentScreen == "add_project" -> {
    AddProjectScreen(
    clients = clients,
    onBackToDashboard = {
        currentScreen = "home"
    },
onProjectSaved = { clientId, projectName, projectType, eventDate, location, deadline, status ->
    scope.launch {
        try {
            val savedToken = TokenManager.getToken(context)
            if (savedToken.isNullOrBlank()) {
                projectErrorMessage = "Your session has expired. Please login again."
                showProjectErrorDialog = true
                return@launch
            }

            val response = ProjectApi.createProject(
                token = savedToken,
                clientId = clientId,
                title = projectName,
                projectType = projectType,
                eventDate = ProjectApi.apiDate(eventDate),
                eventLocation = location,
                deadline = ProjectApi.apiDate(deadline),
                status = status
            )

            if (response.success) {
                val refreshed = ProjectApi.getProjects(savedToken)
                if (refreshed.success) {
                    projects = refreshed.projects?.map { apiProject ->
                        Project(
                            id = apiProject.id,
                            clientId = apiProject.client_id,
                            projectName = apiProject.title,
                            projectType = apiProject.project_type ?: "",
                            eventDate = ProjectApi.displayDate(apiProject.event_date),
                            location = apiProject.event_location ?: "",
                            deadline = ProjectApi.displayDate(apiProject.deadline),
                            status = apiProject.status ?: "Booked"
                        )
                    } ?: emptyList()
                }
                showProjectAddedDialog = true
            } else {
                projectErrorMessage = response.message
                showProjectErrorDialog = true
            }
        } catch (e: Exception) {
            println("CREATE PROJECT API ERROR: ${e.message}")
            projectErrorMessage = "Unable to create project. Please check your connection and try again."
            showProjectErrorDialog = true
        }
    }
}
)
}

isAuthenticated && currentScreen == "projects" -> {
    ProjectListScreen(
    projects = projects,
    clients = clients,
    onAddProject = {
        currentScreen = "add_project"
    },
onProjectClick = { projectId ->
    selectedProjectId = projectId
    currentScreen = "view_project"
},
onBackToDashboard = {
    currentScreen = "home"
}
)
}

isAuthenticated && currentScreen == "clients" -> {
    ClientListScreen(
    clients = clients,
    onAddClient = {
        currentScreen = "add_client"
    },
onViewClient = { clientId ->
    viewingClientId = clientId
    currentScreen = "view_client"
},
onEditClient = { clientId ->
    editingClientId = clientId
    currentScreen = "edit_client"
},
onDeleteClient = { clientId ->
    deletingClientId = clientId
},
onBackToDashboard = {
    currentScreen = "home"
}
)
}

isAuthenticated && currentScreen == "profile" -> {
    ProfileScreen(
        profile = profile,
        onBack = {
            currentScreen = "home"
        },
        onProfileUpdated = { updatedProfile ->
            profile = updatedProfile
            showProfileUpdatedDialog = true
        },
        onError = { message ->
            profileErrorMessage = message
            showProfileErrorDialog = true
        }
    )
}

isAuthenticated && currentScreen == "notifications" -> {
    NotificationsScreen(
        refreshKey = 0,
        onBack = {
            currentScreen = "home"
        }
    )
}

isAuthenticated -> {
    HomeScreen(
    clients = clients,
    projects = projects,
    onClientsClick = {
        currentScreen = "clients"
    },
onProjectsClick = {
    currentScreen = "projects"
},
onSchedulingClick = {
    currentScreen = "scheduling"
},
onDeadlineRemindersClick = {
    currentScreen = "deadline_reminders"
},
onShootRemindersClick = {
    currentScreen = "shoot_reminders"
},
onPendingTasksClick = {
    currentScreen = "pending_tasks"
},
onProjectHistoryClick = {
    currentScreen = "project_history"
},
onSummaryDashboardClick = {
    currentScreen = "summary_dashboard"
},
onNotificationsClick = {
    currentScreen = "notifications"
},
onProfileClick = {
    currentScreen = "profile"
},
onLogout = {
    showLogoutDialog = true
}
)
}

currentScreen == "register" -> {
    RegisterScreen(
    onBackToLogin = {
        currentScreen = "login"
    },
onRegistrationSuccess = { fullName, email, password ->
    scope.launch {
        try {
            val response = AuthApi.register(
                fullName = fullName,
                email = email,
                password = password
            )

            if (response.success) {
                showRegistrationSuccessDialog = true
            } else {
                authErrorMessage = response.message
                showAuthErrorDialog = true
            }

        } catch (e: retrofit2.HttpException) {

            authErrorMessage = when (e.code()) {
                409 -> "An account with this email already exists."
                400 -> "Please check your registration details."
                else -> "Registration failed. Please try again."
            }

            showAuthErrorDialog = true

        } catch (e: Exception) {

            Log.e("CreatorLogAuth", "REGISTRATION ERROR", e)

            authErrorMessage =
                "${e::class.simpleName}: ${e.message ?: "Unknown error"}"

            showAuthErrorDialog = true
        }
    }
}
)
}

else -> {
    LoginScreen(
    onRegisterClick = {
        currentScreen = "register"
    },
onLoginSuccess = { email, password ->
    scope.launch {
        try {
            val response = AuthApi.login(
            email = email,
            password = password
        )

        if (response.success && !response.token.isNullOrBlank()) {
            TokenManager.saveToken(
            context = context,
            token = response.token
        )

        try {
            val protectedResponse = AuthApi.getProtected(
            response.token
        )

        if (protectedResponse.success) {
            println(
            "PROTECTED TEST SUCCESS: ${protectedResponse.message}"
        )
        println(
        "AUTHENTICATED USER ID: ${protectedResponse.user.id}"
    )
    println(
    "AUTHENTICATED EMAIL: ${protectedResponse.user.email}"
)
} else {
println("PROTECTED TEST FAILED")
}
} catch (e: Exception) {
println(
"PROTECTED TEST ERROR: ${e.message}"
)
}

showLoginSuccessDialog = true
} else {
authErrorMessage = response.message
showAuthErrorDialog = true
}
} catch (e: Exception) {
    Log.e("CreatorLogAuth", "LOGIN ERROR", e)

    authErrorMessage =
        "${e::class.simpleName}: ${e.message ?: "Unknown error"}"

    showAuthErrorDialog = true
}
}
}
)
}
}

if (showLoginSuccessDialog) {

    LaunchedEffect(Unit) {
        delay(1500)
        showLoginSuccessDialog = false
        isAuthenticated = true
        currentScreen = "home"
    }

AlertDialog(
onDismissRequest = {
    // Prevent accidental dismissal.
},
title = {
    Text(
    text = "✓  Login Successful",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(
    text = "Welcome back to CreatorLog!"
)
},
confirmButton = {}
)
}

// REGISTRATION SUCCESS DIALOG
if (showRegistrationSuccessDialog) {

    LaunchedEffect(Unit) {
        delay(1500)
        showRegistrationSuccessDialog = false
        currentScreen = "login"
    }

AlertDialog(
onDismissRequest = {
    // Prevent accidental dismissal.
},
title = {
    Text(
    text = "✓  Registration Successful",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(
    text = "Your CreatorLog account has been created successfully."
)
},
confirmButton = {}
)
}

// CLIENT ADDED SUCCESS DIALOG
if (showClientAddedDialog) {

    LaunchedEffect(Unit) {
        delay(1500)
        showClientAddedDialog = false
    }

AlertDialog(
onDismissRequest = {
    // Prevent accidental dismissal.
},
title = {
    Text(
    text = "✓  Client Added",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(
    text = "The client has been added successfully."
)
},
confirmButton = {}
)
}

// CLIENT API ERROR DIALOG
if (showClientErrorDialog) {
    AlertDialog(
        onDismissRequest = {
            showClientErrorDialog = false
        },
        title = {
            Text(
                text = "Client Error",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = clientErrorMessage)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showClientErrorDialog = false
                }
            ) {
                Text("OK")
            }
        }
    )
}

// CLIENT UPDATED SUCCESS DIALOG
if (showClientUpdatedDialog) {

    LaunchedEffect(Unit) {
        delay(1500)
        showClientUpdatedDialog = false
        currentScreen = "clients"
        editingClientId = null
    }

AlertDialog(
onDismissRequest = {
    // Prevent accidental dismissal.
},
title = {
    Text(
    text = "✓  Client Updated",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(
    text = "Client information has been updated successfully."
)
},
confirmButton = {}
)
}

// DELETE CLIENT CONFIRMATION DIALOG
if (deletingClientId != null && !showClientDeletedDialog) {
    val clientToDelete = clients.find { it.id == deletingClientId }

    if (clientToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                deletingClientId = null
            },
            title = {
                Text(
                    text = "Delete Client?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${clientToDelete.fullName}?\n\nThis action cannot be undone."
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deletingClientId = null
                    }
                ) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val savedToken = TokenManager.getToken(context)

                                if (savedToken.isNullOrBlank()) {
                                    clientErrorMessage = "Your session has expired. Please login again."
                                    showClientErrorDialog = true
                                    deletingClientId = null
                                    return@launch
                                }

                                val response = ClientApi.deleteClient(
                                    token = savedToken,
                                    clientId = clientToDelete.id
                                )

                                if (response.success) {
                                    val refreshedResponse = ClientApi.getClients(savedToken)

                                    if (refreshedResponse.success) {
                                        clients = refreshedResponse.clients?.map { apiClient ->
                                            Client(
                                                id = apiClient.id,
                                                fullName = apiClient.full_name,
                                                contactNumber = apiClient.phone ?: "",
                                                email = apiClient.email ?: "",
                                                notes = apiClient.notes ?: ""
                                            )
                                        } ?: emptyList()
                                    }

                                    deletingClientId = null
                                    showClientDeletedDialog = true
                                    currentScreen = "clients"
                                } else {
                                    deletingClientId = null
                                    clientErrorMessage = response.message
                                    showClientErrorDialog = true
                                }
                            } catch (e: Exception) {
                                println("DELETE CLIENT API ERROR: ${e.message}")
                                deletingClientId = null
                                clientErrorMessage = "Unable to delete client. Please check your connection and try again."
                                showClientErrorDialog = true
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            }
        )
    }
}

// CLIENT DELETED SUCCESS DIALOG
if (showClientDeletedDialog) {

    LaunchedEffect(Unit) {
        delay(1500)
        showClientDeletedDialog = false
        currentScreen = "clients"
    }

AlertDialog(
onDismissRequest = {
    // Prevent accidental dismissal.
},
title = {
    Text(
    text = "✓  Client Deleted",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(
    text = "The client has been deleted successfully."
)
},
confirmButton = {}
)
}

// PROJECT ADDED SUCCESS DIALOG
if (showProjectAddedDialog) {

    LaunchedEffect(Unit) {
        delay(1500)
        showProjectAddedDialog = false
        currentScreen = "home"
    }

AlertDialog(
onDismissRequest = {
    // Prevent accidental dismissal.
},
title = {
    Text(
    text = "✓  Project Added",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(
    text = "The project has been created successfully."
)
},
confirmButton = {}
)
}

// PROJECT UPDATED SUCCESS DIALOG
if (showProjectUpdatedDialog) {

    LaunchedEffect(Unit) {
        delay(1500)
        showProjectUpdatedDialog = false
        currentScreen = "view_project"
    }

AlertDialog(
onDismissRequest = {
    // Prevent accidental dismissal.
},
title = {
    Text(
    text = "✓  Project Updated",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(
    text = "Project information has been updated successfully."
)
},
confirmButton = {}
)
}

// PROJECT STATUS UPDATED SUCCESS DIALOG
if (showProjectStatusUpdatedDialog) {

    LaunchedEffect(Unit) {
        delay(1500)
        showProjectStatusUpdatedDialog = false
        currentScreen = "view_project"
    }

AlertDialog(
onDismissRequest = {
    // Prevent accidental dismissal.
},
title = {
    Text(
    text = "✓  Status Updated",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(
    text = "Project status has been updated successfully."
)
},
confirmButton = {}
)
}

// PROJECT API ERROR DIALOG
if (showProjectErrorDialog) {
    AlertDialog(
        onDismissRequest = { showProjectErrorDialog = false },
        title = {
            Text(
                text = "Project Error",
                fontWeight = FontWeight.Bold
            )
        },
        text = { Text(text = projectErrorMessage) },
        confirmButton = {
            TextButton(onClick = { showProjectErrorDialog = false }) {
                Text("OK")
            }
        }
    )
}

// AUTHENTICATION ERROR DIALOG
if (showAuthErrorDialog) {
    AlertDialog(
    onDismissRequest = {
        showAuthErrorDialog = false
    },
title = {
    Text(
    text = "Login / Registration Error",
    fontWeight = FontWeight.Bold
)
},
text = {
    Text(text = authErrorMessage)
},
confirmButton = {
    TextButton(
    onClick = {
        showAuthErrorDialog = false
    }
) {
    Text("OK")
}
}
)
}

// PROFILE UPDATED SUCCESS DIALOG
if (showProfileUpdatedDialog) {
    LaunchedEffect(Unit) {
        delay(1500)
        showProfileUpdatedDialog = false
    }

    AlertDialog(
        onDismissRequest = {
            // Prevent accidental dismissal.
        },
        title = {
            Text(
                text = "✓  Profile Updated",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Your account information has been updated successfully."
            )
        },
        confirmButton = {}
    )
}

// PROFILE ERROR DIALOG
if (showProfileErrorDialog) {
    AlertDialog(
        onDismissRequest = {
            showProfileErrorDialog = false
        },
        title = {
            Text(
                text = "Profile Error",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = profileErrorMessage)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showProfileErrorDialog = false
                }
            ) {
                Text("OK")
            }
        }
    )
}

// LOGOUT CONFIRMATION DIALOG
if (showLogoutDialog) {
    AlertDialog(
    onDismissRequest = {
        showLogoutDialog = false
    },
title = {
    Text(
    text = "Logout"
)
},
text = {
    Text(
    text = "Are you sure you want to logout?"
)
},
dismissButton = {
    TextButton(
    onClick = {
        showLogoutDialog = false
    }
) {
    Text("Cancel")
}
},
confirmButton = {
    Button(
    onClick = {
        showLogoutDialog = false
        TokenManager.clearToken(context)
        isAuthenticated = false
        currentScreen = "login"
    }
) {
    Text("Logout")
}
}
)
}
}
}
}

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: (String, String) -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    var backendStatus by remember { mutableStateOf("Checking backend...") }

    LaunchedEffect(Unit) {
    try {
        val response = HealthApi.checkBackend()

        backendStatus = if (response.success) {
            "✓ Backend Connected"
        } else {
            "✗ Backend Error"
        }
    } catch (e: Exception) {
        backendStatus = "✗ Backend Connection Failed"
    }
}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "CreatorLog",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Photography & Video Project Management",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

Text(
    text = backendStatus,
    style = MaterialTheme.typography.bodySmall,
    fontWeight = FontWeight.SemiBold
)

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Email")
            },
            singleLine = true,
            isError = emailError.isNotEmpty()
        )

        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Password")
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError.isNotEmpty()
        )

        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {

                emailError = ""
                passwordError = ""

                var valid = true

                if (email.isBlank()) {
                    emailError = "Email is required."
                    valid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = "Enter a valid email address."
                    valid = false
                }

                if (password.isBlank()) {
                    passwordError = "Password is required."
                    valid = false
                } else if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters."
                    valid = false
                }

                if (valid) {
                    onLoginSuccess(email.trim(), password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Login",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = {
                // Forgot Password will be implemented later.
            }
        ) {
            Text("Forgot Password?")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Don't have an account?",
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(
            onClick = onRegisterClick
        ) {
            Text(
                text = "Create an Account",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegistrationSuccess: (String, String, String) -> Unit
) {

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var fullNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set up your CreatorLog account",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                fullNameError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Full Name")
            },
            singleLine = true,
            isError = fullNameError.isNotEmpty()
        )

        if (fullNameError.isNotEmpty()) {
            Text(
                text = fullNameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Email")
            },
            singleLine = true,
            isError = emailError.isNotEmpty()
        )

        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Password")
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError.isNotEmpty()
        )

        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Confirm Password")
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmPasswordError.isNotEmpty()
        )

        if (confirmPasswordError.isNotEmpty()) {
            Text(
                text = confirmPasswordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                fullNameError = ""
                emailError = ""
                passwordError = ""
                confirmPasswordError = ""

                var valid = true

                if (fullName.isBlank()) {
                    fullNameError = "Full name is required."
                    valid = false
                }

                if (email.isBlank()) {
                    emailError = "Email is required."
                    valid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = "Enter a valid email address."
                    valid = false
                }

                if (password.isBlank()) {
                    passwordError = "Password is required."
                    valid = false
                } else if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters."
                    valid = false
                }

                if (confirmPassword.isBlank()) {
                    confirmPasswordError = "Please confirm your password."
                    valid = false
                } else if (password != confirmPassword) {
                    confirmPasswordError = "Passwords do not match."
                    valid = false
                }

                if (valid) {
                    onRegistrationSuccess(
                        fullName.trim(),
                        email.trim(),
                        password
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Create Account",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Login")
        }
    }
}

@Composable
fun HomeScreen(
    clients: List<Client>,
    projects: List<Project>,
    onClientsClick: () -> Unit,
    onProjectsClick: () -> Unit,
    onSchedulingClick: () -> Unit,
    onDeadlineRemindersClick: () -> Unit,
    onShootRemindersClick: () -> Unit,
    onPendingTasksClick: () -> Unit,
    onProjectHistoryClick: () -> Unit,
    onSummaryDashboardClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .imePadding()
    ) {

        Text(
            text = "CreatorLog",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardSummaryCard(
                title = "Clients",
                value = clients.size.toString(),
                modifier = Modifier.weight(1f)
            )

            DashboardSummaryCard(
                title = "Projects",
                value = projects.size.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardSummaryCard(
                title = "Completed",
                value = "0",
                modifier = Modifier.weight(1f)
            )

            DashboardSummaryCard(
                title = "Pending Tasks",
                value = "0",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onClientsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Clients",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onProjectsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Projects",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSchedulingClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Scheduling",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Reminders & Reports",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onDeadlineRemindersClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Deadline Reminders") }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onShootRemindersClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Shoot Reminders") }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onPendingTasksClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Pending Task Reminders") }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onProjectHistoryClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Project History") }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onSummaryDashboardClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Project Summary Dashboard") }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNotificationsClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Notifications") }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onProfileClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Account / Profile") }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Logout",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProfileScreen(
    profile: ProfileUser?,
    onBack: () -> Unit,
    onProfileUpdated: (ProfileUser) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fullName by remember(profile?.id) {
        mutableStateOf(profile?.full_name ?: "")
    }
    var email by remember(profile?.id) {
        mutableStateOf(profile?.email ?: "")
    }

    var fullNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var showSaveConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Account / Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Manage your CreatorLog account information."
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                fullNameError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Full Name")
            },
            singleLine = true,
            isError = fullNameError.isNotEmpty()
        )

        if (fullNameError.isNotEmpty()) {
            Text(
                text = fullNameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Email")
            },
            singleLine = true,
            isError = emailError.isNotEmpty()
        )

        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                fullNameError = ""
                emailError = ""

                var valid = true

                if (fullName.isBlank()) {
                    fullNameError = "Full name is required."
                    valid = false
                }

                if (email.isBlank()) {
                    emailError = "Email is required."
                    valid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                    emailError = "Enter a valid email address."
                    valid = false
                }

                if (valid) {
                    showSaveConfirmation = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Save Changes",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }

    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showSaveConfirmation = false
            },
            title = {
                Text(
                    text = "Save Changes?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to update your account information?"
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveConfirmation = false

                        scope.launch {
                            val token = TokenManager.getToken(context)

                            if (token.isNullOrBlank()) {
                                onError("Your session has expired. Please login again.")
                                return@launch
                            }

                            try {
                                val response = ProfileApi.updateProfile(
                                    token = token,
                                    fullName = fullName.trim(),
                                    email = email.trim()
                                )

                                if (response.success && response.user != null) {
                                    if (!response.token.isNullOrBlank()) {
                                        TokenManager.saveToken(
                                            context = context,
                                            token = response.token
                                        )
                                    }

                                    onProfileUpdated(response.user)
                                } else {
                                    onError(
                                        if (response.message.isNotBlank()) {
                                            response.message
                                        } else {
                                            "Unable to update your profile."
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                println("UPDATE PROFILE API ERROR: ${e.message}")
                                onError(
                                    "Unable to update your profile. Please check your connection and try again."
                                )
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        )
    }
}

@Composable
fun DashboardSummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ClientListScreen(
    clients: List<Client>,
    onAddClient: () -> Unit,
    onViewClient: (Int) -> Unit,
    onEditClient: (Int) -> Unit,
    onDeleteClient: (Int) -> Unit,
    onBackToDashboard: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .imePadding()
    ) {

        Text(
            text = "Clients",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Manage your client profiles",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddClient,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "+ Add Client",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (clients.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No clients yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add your first client to get started.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            clients.forEach { client ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = client.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = client.contactNumber,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (client.email.isNotBlank()) {
                            Text(
                                text = client.email,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (client.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Notes: ${client.notes}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onViewClient(client.id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Client")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onEditClient(client.id)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit")
                            }

                            OutlinedButton(
                                onClick = {
                                    onDeleteClient(client.id)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBackToDashboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
fun ViewClientScreen(
    client: Client,
    onBackToClients: () -> Unit,
    onEditClient: () -> Unit,
    onDeleteClient: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {

        Text(
            text = "Client Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    text = "Full Name",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = client.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Contact Number",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = client.contactNumber,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = if (client.email.isBlank()) "Not provided" else client.email,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = if (client.notes.isBlank()) "No notes" else client.notes,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onEditClient,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Edit Client")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onDeleteClient,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Delete Client")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToClients,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Clients")
        }
    }
}

@Composable
fun AddClientScreen(
    onBackToClients: () -> Unit,
    onClientSaved: (String, String, String, String) -> Unit
) {

    var fullName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var fullNameError by remember { mutableStateOf("") }
    var contactNumberError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {

        Text(
            text = "Add Client",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Create a new client profile",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                fullNameError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full Name *") },
            singleLine = true,
            isError = fullNameError.isNotEmpty()
        )

        if (fullNameError.isNotEmpty()) {
            Text(
                text = fullNameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contactNumber,
            onValueChange = {
                contactNumber = it
                contactNumberError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contact Number *") },
            singleLine = true,
            isError = contactNumberError.isNotEmpty()
        )

        if (contactNumberError.isNotEmpty()) {
            Text(
                text = contactNumberError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            isError = emailError.isNotEmpty()
        )

        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = { Text("Notes") },
            minLines = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                fullNameError = ""
                contactNumberError = ""
                emailError = ""

                var valid = true

                if (fullName.isBlank()) {
                    fullNameError = "Full name is required."
                    valid = false
                }

                if (contactNumber.isBlank()) {
                    contactNumberError = "Contact number is required."
                    valid = false
                }

                if (email.isNotBlank() &&
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                ) {
                    emailError = "Enter a valid email address."
                    valid = false
                }

                if (valid) {
                    onClientSaved(
                        fullName.trim(),
                        contactNumber.trim(),
                        email.trim(),
                        notes.trim()
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Save Client",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToClients,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
fun EditClientScreen(
    client: Client,
    onBackToClients: () -> Unit,
    onClientUpdated: (Client) -> Unit
) {

    var fullName by remember { mutableStateOf(client.fullName) }
    var contactNumber by remember { mutableStateOf(client.contactNumber) }
    var email by remember { mutableStateOf(client.email) }
    var notes by remember { mutableStateOf(client.notes) }

    var fullNameError by remember { mutableStateOf("") }
    var contactNumberError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {

        Text(
            text = "Edit Client",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Update client information",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                fullNameError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full Name *") },
            singleLine = true,
            isError = fullNameError.isNotEmpty()
        )

        if (fullNameError.isNotEmpty()) {
            Text(
                text = fullNameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contactNumber,
            onValueChange = {
                contactNumber = it
                contactNumberError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contact Number *") },
            singleLine = true,
            isError = contactNumberError.isNotEmpty()
        )

        if (contactNumberError.isNotEmpty()) {
            Text(
                text = contactNumberError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            isError = emailError.isNotEmpty()
        )

        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = { Text("Notes") },
            minLines = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                fullNameError = ""
                contactNumberError = ""
                emailError = ""

                var valid = true

                if (fullName.isBlank()) {
                    fullNameError = "Full name is required."
                    valid = false
                }

                if (contactNumber.isBlank()) {
                    contactNumberError = "Contact number is required."
                    valid = false
                }

                if (
                    email.isNotBlank() &&
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                ) {
                    emailError = "Enter a valid email address."
                    valid = false
                }

                if (valid) {
                    onClientUpdated(
                        Client(
                            id = client.id,
                            fullName = fullName.trim(),
                            contactNumber = contactNumber.trim(),
                            email = email.trim(),
                            notes = notes.trim()
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Save Changes",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToClients,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}



@Composable
fun ProjectListScreen(
    projects: List<Project>,
    clients: List<Client>,
    onAddProject: () -> Unit,
    onProjectClick: (Int) -> Unit,
    onBackToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Projects",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Manage your photography and video projects",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddProject,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "+ Add Project",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (projects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No projects yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create your first project to get started.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            projects.forEach { project ->
                val clientName = clients
                    .find { it.id == project.clientId }
                    ?.fullName
                    ?: "Unknown client"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            onProjectClick(project.id)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = project.projectName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Client: $clientName",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Type: ${project.projectType}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Event Date: ${project.eventDate}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Deadline: ${project.deadline}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Status: ${project.status}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onBackToDashboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
fun ProjectDetailsScreen(
    project: Project,
    clientName: String,
    onEditProject: () -> Unit,
    onChangeStatus: () -> Unit,
    onOpenChecklist: () -> Unit,
    onBackToProjects: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var projectFee by remember(project.id) { mutableStateOf(0.0) }
    var payments by remember(project.id) { mutableStateOf<List<PaymentRecord>>(emptyList()) }
    var financeLoading by remember(project.id) { mutableStateOf(true) }
    var showSetFeeDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showPaymentHistoryDialog by remember { mutableStateOf(false) }
    var showFeeSuccessDialog by remember { mutableStateOf(false) }
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }
    var showPaymentConfirmDialog by remember { mutableStateOf(false) }
    var pendingPaymentAmount by remember { mutableStateOf(0.0) }
    var financeError by remember { mutableStateOf("") }

    LaunchedEffect(project.id) {
        val token = TokenManager.getToken(context)
        if (token.isNullOrBlank()) {
            financeError = "Your session has expired. Please login again."
            financeLoading = false
        } else {
            try {
                val balanceResponse = FinanceApi.getBalance(token, project.id)
                val paymentResponse = FinanceApi.getPayments(token, project.id)

                if (balanceResponse.success) {
                    projectFee = balanceResponse.finance?.total_fee ?: 0.0
                } else {
                    financeError = balanceResponse.message
                }

                if (paymentResponse.success) {
                    payments = paymentResponse.payments?.map {
                        PaymentRecord(
                            amount = it.amount,
                            date = it.paid_at?.take(10) ?: ""
                        )
                    } ?: emptyList()
                } else if (financeError.isBlank()) {
                    financeError = paymentResponse.message
                }
            } catch (e: Exception) {
                financeError = "Unable to load finance data from the server."
            } finally {
                financeLoading = false
            }
        }
    }

    val totalPaid = payments.sumOf { it.amount }
    val remainingBalance = (projectFee - totalPaid).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Project Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "View project information and payment details",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = project.projectName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                ProjectDetailRow("Client", clientName)
                ProjectDetailRow("Project Type", project.projectType)
                ProjectDetailRow("Event Date", project.eventDate)
                ProjectDetailRow("Location", project.location)
                ProjectDetailRow("Deadline", project.deadline)

                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onChangeStatus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = project.status,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Finance",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Payment Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProjectDetailRow("Project Fee", formatCurrency(projectFee))
                ProjectDetailRow("Total Paid", formatCurrency(totalPaid))
                ProjectDetailRow("Remaining Balance", formatCurrency(remainingBalance))

                Button(
                    onClick = {
                        financeError = ""
                        showSetFeeDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set Project Fee")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        financeError = ""
                        showPaymentDialog = true
                    },
                    enabled = remainingBalance > 0.0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Record Payment")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showPaymentHistoryDialog = true },
                    enabled = payments.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Payment History (${payments.size})")
                }

                if (financeError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = financeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onOpenChecklist,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Project Checklist",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onEditProject,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Edit Project",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToProjects,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Projects")
        }
    }

    if (showSetFeeDialog) {
        var feeText by remember { mutableStateOf(if (projectFee > 0.0) projectFee.toString() else "") }
        var feeError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSetFeeDialog = false },
            title = {
                Text("Set Project Fee", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = {
                            feeText = it
                            feeError = ""
                        },
                        label = { Text("Project Fee (₱)") },
                        singleLine = true,
                        isError = feeError.isNotEmpty()
                    )

                    if (feeError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = feeError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetFeeDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val fee = feeText.toDoubleOrNull()
                        if (fee == null || fee < 0.0) {
                            feeError = "Enter a valid project fee."
                        } else if (fee < totalPaid) {
                            feeError = "Project fee cannot be lower than total paid (${formatCurrency(totalPaid)})."
                        } else {
                            scope.launch {
                                val token = TokenManager.getToken(context)
                                if (token.isNullOrBlank()) {
                                    financeError = "Your session has expired. Please login again."
                                } else {
                                    try {
                                        val response = FinanceApi.updateFee(token, project.id, fee)
                                        if (response.success) {
                                            projectFee = fee
                                            showSetFeeDialog = false
                                            showFeeSuccessDialog = true
                                        } else {
                                            financeError = response.message
                                        }
                                    } catch (e: Exception) {
                                        financeError = "Unable to save project fee."
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        )
    }

    if (showPaymentDialog) {
        var paymentText by remember { mutableStateOf("") }
        var paymentError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = {
                Text("Record Payment", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Remaining balance: ${formatCurrency(remainingBalance)}")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = paymentText,
                        onValueChange = {
                            paymentText = it
                            paymentError = ""
                        },
                        label = { Text("Payment Amount (₱)") },
                        singleLine = true,
                        isError = paymentError.isNotEmpty()
                    )

                    if (paymentError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = paymentError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = paymentText.toDoubleOrNull()
                        when {
                            amount == null || amount <= 0.0 -> {
                                paymentError = "Enter a valid payment amount."
                            }
                            amount > remainingBalance -> {
                                paymentError = "Payment cannot exceed the remaining balance."
                            }
                            else -> {
                                pendingPaymentAmount = amount
                                showPaymentDialog = false
                                showPaymentConfirmDialog = true
                            }
                        }
                    }
                ) {
                    Text("Continue")
                }
            }
        )
    }

    if (showPaymentConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentConfirmDialog = false },
            title = {
                Text("Confirm Payment", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Record a payment of ${formatCurrency(pendingPaymentAmount)} for this project?")
            },
            dismissButton = {
                TextButton(onClick = { showPaymentConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val token = TokenManager.getToken(context)
                            if (token.isNullOrBlank()) {
                                financeError = "Your session has expired. Please login again."
                            } else {
                                try {
                                    val response = FinanceApi.createPayment(
                                        token = token,
                                        projectId = project.id,
                                        amount = pendingPaymentAmount
                                    )

                                    if (response.success) {
                                        val refreshed = FinanceApi.getPayments(token, project.id)
                                        payments = refreshed.payments?.map {
                                            PaymentRecord(
                                                amount = it.amount,
                                                date = it.paid_at?.take(10) ?: ""
                                            )
                                        } ?: emptyList()

                                        showPaymentConfirmDialog = false
                                        showPaymentSuccessDialog = true
                                        pendingPaymentAmount = 0.0
                                    } else {
                                        financeError = response.message
                                    }
                                } catch (e: Exception) {
                                    financeError = "Unable to record payment."
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            }
        )
    }

    if (showPaymentHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentHistoryDialog = false },
            title = {
                Text("Payment History", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    payments.forEachIndexed { index, payment ->
                        Text(
                            text = "Payment ${index + 1}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Amount: ${formatCurrency(payment.amount)}")
                        Text("Date: ${payment.date}")
                        if (index < payments.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentHistoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showFeeSuccessDialog) {
        LaunchedEffect(Unit) {
            delay(1200)
            showFeeSuccessDialog = false
        }

        AlertDialog(
            onDismissRequest = {},
            title = { Text("✓  Project Fee Saved", fontWeight = FontWeight.Bold) },
            text = { Text("The project fee has been saved successfully.") },
            confirmButton = {}
        )
    }

    if (showPaymentSuccessDialog) {
        LaunchedEffect(Unit) {
            delay(1200)
            showPaymentSuccessDialog = false
        }

        AlertDialog(
            onDismissRequest = {},
            title = { Text("✓  Payment Recorded", fontWeight = FontWeight.Bold) },
            text = { Text("The payment has been recorded successfully.") },
            confirmButton = {}
        )
    }
}

@Composable
fun ProjectDetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProjectStatusScreen(
    project: Project,
    onBackToProjectDetails: () -> Unit,
    onStatusUpdated: (String) -> Unit
) {
    val statuses = listOf(
        "Booked",
        "Upcoming",
        "Shooting",
        "Editing",
        "Review",
        "Delivered"
    )

    var selectedStatus by remember { mutableStateOf(project.status) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Project Status",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Update the current progress of your project.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = project.projectName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Current status: ${project.status}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Select Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        statuses.forEach { status ->
            if (selectedStatus == status) {
                Button(
                    onClick = { selectedStatus = status },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("✓  $status")
                }
            } else {
                OutlinedButton(
                    onClick = { selectedStatus = status },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(status)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onStatusUpdated(selectedStatus) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Save Status",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToProjectDetails,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Project Details")
        }
    }
}

@Composable
fun EditProjectScreen(
    project: Project,
    clients: List<Client>,
    onBackToProjectDetails: () -> Unit,
    onProjectUpdated: (Project) -> Unit
) {
    var selectedClientId by remember { mutableStateOf<Int?>(project.clientId) }
    var projectName by remember { mutableStateOf(project.projectName) }
    var projectType by remember { mutableStateOf(project.projectType) }
    var eventDate by remember { mutableStateOf(project.eventDate) }
    var location by remember { mutableStateOf(project.location) }
    var deadline by remember { mutableStateOf(project.deadline) }

    var clientError by remember { mutableStateOf("") }
    var projectNameError by remember { mutableStateOf("") }
    var eventDateError by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf("") }
    var deadlineError by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFormat = remember {
        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    }

    fun showDatePicker(
        currentValue: String,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        if (currentValue.isNotBlank()) {
            try {
                calendar.time = dateFormat.parse(currentValue) ?: Calendar.getInstance().time
            } catch (_: Exception) {
                // Keep today's date if the existing value cannot be parsed.
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Edit Project",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Update project information",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Client *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        clients.forEach { client ->
            if (selectedClientId == client.id) {
                Button(
                    onClick = {
                        selectedClientId = client.id
                        clientError = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("✓ ${client.fullName}")
                }
            } else {
                OutlinedButton(
                    onClick = {
                        selectedClientId = client.id
                        clientError = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(client.fullName)
                }
            }
        }

        if (clientError.isNotEmpty()) {
            Text(
                text = clientError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = projectName,
            onValueChange = {
                projectName = it
                projectNameError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Project Name *") },
            singleLine = true,
            isError = projectNameError.isNotEmpty()
        )

        if (projectNameError.isNotEmpty()) {
            Text(
                text = projectNameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Project Type",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (projectType == "Photography") {
                Button(
                    onClick = { projectType = "Photography" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Photography")
                }
            } else {
                OutlinedButton(
                    onClick = { projectType = "Photography" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Photography")
                }
            }

            if (projectType == "Video") {
                Button(
                    onClick = { projectType = "Video" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Video")
                }
            } else {
                OutlinedButton(
                    onClick = { projectType = "Video" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Video")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = eventDate,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Event Date *") },
                singleLine = true,
                readOnly = true,
                isError = eventDateError.isNotEmpty()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        showDatePicker(eventDate) {
                            eventDate = it
                            eventDateError = ""
                        }
                    }
            )
        }

        if (eventDateError.isNotEmpty()) {
            Text(
                text = eventDateError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = {
                location = it
                locationError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Location *") },
            singleLine = true,
            isError = locationError.isNotEmpty()
        )

        if (locationError.isNotEmpty()) {
            Text(
                text = locationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = deadline,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Deadline *") },
                singleLine = true,
                readOnly = true,
                isError = deadlineError.isNotEmpty()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        showDatePicker(deadline) {
                            deadline = it
                            deadlineError = ""
                        }
                    }
            )
        }

        if (deadlineError.isNotEmpty()) {
            Text(
                text = deadlineError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Status: ${project.status}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                clientError = ""
                projectNameError = ""
                eventDateError = ""
                locationError = ""
                deadlineError = ""

                var valid = true

                if (selectedClientId == null) {
                    clientError = "Please select a client."
                    valid = false
                }

                if (projectName.isBlank()) {
                    projectNameError = "Project name is required."
                    valid = false
                }

                if (eventDate.isBlank()) {
                    eventDateError = "Event date is required."
                    valid = false
                }

                if (location.isBlank()) {
                    locationError = "Location is required."
                    valid = false
                }

                if (deadline.isBlank()) {
                    deadlineError = "Deadline is required."
                    valid = false
                }

                if (valid) {
                    onProjectUpdated(
                        Project(
                            id = project.id,
                            clientId = selectedClientId!!,
                            projectName = projectName.trim(),
                            projectType = projectType,
                            eventDate = eventDate.trim(),
                            location = location.trim(),
                            deadline = deadline.trim(),
                            status = project.status
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Save Changes",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToProjectDetails,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun AddProjectScreen(
    clients: List<Client>,
    onBackToDashboard: () -> Unit,
    onProjectSaved: (
        Int,
        String,
        String,
        String,
        String,
        String,
        String
    ) -> Unit
) {
    var selectedClientId by remember { mutableStateOf<Int?>(null) }
    var projectName by remember { mutableStateOf("") }
    var projectType by remember { mutableStateOf("Photography") }
    var eventDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    var clientError by remember { mutableStateOf("") }
    var projectNameError by remember { mutableStateOf("") }
    var eventDateError by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf("") }
    var deadlineError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dateFormat = remember {
        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    }

    fun showDatePicker(
        currentValue: String,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        if (currentValue.isNotBlank()) {
            try {
                calendar.time = dateFormat.parse(currentValue) ?: Calendar.getInstance().time
            } catch (_: Exception) {
                // Keep today's date if the existing value cannot be parsed.
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Create Project",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Create a new photography or video project",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Client *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (clients.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No clients available. Add a client first.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            clients.forEach { client ->
                OutlinedButton(
                    onClick = {
                        selectedClientId = client.id
                        clientError = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = if (selectedClientId == client.id) {
                            "✓ ${client.fullName}"
                        } else {
                            client.fullName
                        }
                    )
                }
            }
        }

        if (clientError.isNotEmpty()) {
            Text(
                text = clientError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = projectName,
            onValueChange = {
                projectName = it
                projectNameError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Project Name *") },
            singleLine = true,
            isError = projectNameError.isNotEmpty()
        )

        if (projectNameError.isNotEmpty()) {
            Text(
                text = projectNameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Project Type",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (projectType == "Photography") {
                Button(
                    onClick = { projectType = "Photography" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Photography")
                }
            } else {
                OutlinedButton(
                    onClick = { projectType = "Photography" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Photography")
                }
            }

            if (projectType == "Video") {
                Button(
                    onClick = { projectType = "Video" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Video")
                }
            } else {
                OutlinedButton(
                    onClick = { projectType = "Video" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Video")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = eventDate,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Event Date *") },
                placeholder = { Text("Tap to select a date") },
                singleLine = true,
                readOnly = true,
                isError = eventDateError.isNotEmpty()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        showDatePicker(eventDate) {
                            eventDate = it
                            eventDateError = ""
                        }
                    }
            )
        }

        if (eventDateError.isNotEmpty()) {
            Text(
                text = eventDateError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = {
                location = it
                locationError = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Location *") },
            singleLine = true,
            isError = locationError.isNotEmpty()
        )

        if (locationError.isNotEmpty()) {
            Text(
                text = locationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = deadline,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Deadline *") },
                placeholder = { Text("Tap to select a date") },
                singleLine = true,
                readOnly = true,
                isError = deadlineError.isNotEmpty()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        showDatePicker(deadline) {
                            deadline = it
                            deadlineError = ""
                        }
                    }
            )
        }

        if (deadlineError.isNotEmpty()) {
            Text(
                text = deadlineError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Initial Status: Booked",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                clientError = ""
                projectNameError = ""
                eventDateError = ""
                locationError = ""
                deadlineError = ""

                var valid = true

                if (selectedClientId == null) {
                    clientError = "Please select a client."
                    valid = false
                }

                if (projectName.isBlank()) {
                    projectNameError = "Project name is required."
                    valid = false
                }

                if (eventDate.isBlank()) {
                    eventDateError = "Event date is required."
                    valid = false
                }

                if (location.isBlank()) {
                    locationError = "Location is required."
                    valid = false
                }

                if (deadline.isBlank()) {
                    deadlineError = "Deadline is required."
                    valid = false
                }

                if (valid) {
                    onProjectSaved(
                        selectedClientId!!,
                        projectName.trim(),
                        projectType,
                        eventDate.trim(),
                        location.trim(),
                        deadline.trim(),
                        "Booked"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = clients.isNotEmpty()
        ) {
            Text(
                text = "Save Project",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToDashboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
fun SchedulingScreen(
    projects: List<Project>,
    selectedDate: String?,
    onSelectDate: (String) -> Unit,
    onUpcomingShoots: () -> Unit,
    onScheduledActivities: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var displayedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var displayedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    val monthName = remember(displayedMonth, displayedYear) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.YEAR, displayedYear)
                set(Calendar.MONTH, displayedMonth)
                set(Calendar.DAY_OF_MONTH, 1)
            }.time
        )
    }

    val days = remember(displayedMonth, displayedYear) {
        val first = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDay = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val maxDay = first.getActualMaximum(Calendar.DAY_OF_MONTH)
        List(firstDay) { 0 } + (1..maxDay).toList()
    }

    val storageDateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val calendarDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    fun normalizeProjectDate(value: String): String? {
        if (value.isBlank()) return null
        return runCatching {
            val parsed = storageDateFormat.parse(value) ?: return@runCatching null
            calendarDateFormat.format(parsed)
        }.getOrNull()
    }

    val selectedProjects = projects.filter {
        normalizeProjectDate(it.eventDate) == selectedDate
    }

    val shootsByDate = projects.mapNotNull { project ->
        normalizeProjectDate(project.eventDate)?.let { dateKey ->
            dateKey to project
        }
    }.groupBy({ it.first }, { it.second })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Shoot Calendar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "View scheduled shoots by event date.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = {
                if (displayedMonth == Calendar.JANUARY) {
                    displayedMonth = Calendar.DECEMBER
                    displayedYear--
                } else {
                    displayedMonth--
                }
                onSelectDate("")
            }) {
                Text("‹")
            }

            Text(
                text = monthName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(onClick = {
                if (displayedMonth == Calendar.DECEMBER) {
                    displayedMonth = Calendar.JANUARY
                    displayedYear++
                } else {
                    displayedMonth++
                }
                onSelectDate("")
            }) {
                Text("›")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == 0) {
                        Box(modifier = Modifier.weight(1f).height(42.dp))
                    } else {
                        val date = Calendar.getInstance().apply {
                            set(Calendar.YEAR, displayedYear)
                            set(Calendar.MONTH, displayedMonth)
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                        val dateText = calendarDateFormat.format(date.time)
                        val hasShoot = shootsByDate.containsKey(dateText)
                        val isSelected = selectedDate == dateText

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .padding(2.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { onSelectDate(dateText) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontWeight = if (isSelected || hasShoot) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasShoot) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f).height(42.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (!selectedDate.isNullOrEmpty()) {
            Text(
                text = "Selected Date: $selectedDate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedProjects.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No shoots scheduled on this date.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                selectedProjects.forEach { project ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(project.projectName, fontWeight = FontWeight.Bold)
                            Text("Location: ${project.location}")
                            Text("Status: ${project.status}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onUpcomingShoots,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Upcoming Shoots", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onScheduledActivities,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Scheduled Activities", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBackToDashboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
fun UpcomingShootsScreen(
    projects: List<Project>,
    onProjectClick: (Int) -> Unit,
    onBackToScheduling: () -> Unit
) {
    val storageDateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val today = remember {
        Calendar.getInstance().time
    }

    fun parseProjectDate(value: String): Calendar? {
        if (value.isBlank()) return null
        return runCatching {
            Calendar.getInstance().apply {
                time = storageDateFormat.parse(value) ?: return@runCatching null
            }
        }.getOrNull()
    }

    val upcoming = projects
        .filter { project ->
            val eventDate = parseProjectDate(project.eventDate)
            eventDate != null &&
                !eventDate.time.before(today) &&
                project.status != "Delivered"
        }
        .sortedBy { project ->
            parseProjectDate(project.eventDate)?.timeInMillis ?: Long.MAX_VALUE
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Upcoming Shoots",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Projects with upcoming event dates.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (upcoming.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("No upcoming shoots", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Projects with future event dates will appear here.")
                }
            }
        } else {
            upcoming.forEach { project ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { onProjectClick(project.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(project.projectName, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Event Date: ${project.eventDate}")
                        Text("Location: ${project.location}")
                        Text("Status: ${project.status}")
                        Text("Deadline: ${project.deadline}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onBackToScheduling,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Scheduling")
        }
    }
}

@Composable
fun ScheduledActivitiesScreen(
    projects: List<Project>,
    onProjectClick: (Int) -> Unit,
    onBackToScheduling: () -> Unit
) {
    val storageDateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val today = remember {
        Calendar.getInstance().time
    }

    fun parseProjectDate(value: String): Calendar? {
        if (value.isBlank()) return null
        return runCatching {
            Calendar.getInstance().apply {
                time = storageDateFormat.parse(value) ?: return@runCatching null
            }
        }.getOrNull()
    }

    data class ActivityItem(
        val date: String,
        val type: String,
        val project: Project,
        val sortTime: Long
    )

    val activities = projects
        .flatMap { project ->
            buildList {
                parseProjectDate(project.eventDate)?.let { eventDate ->
                    if (!eventDate.time.before(today)) {
                        add(ActivityItem(
                            project.eventDate,
                            "Shoot",
                            project,
                            eventDate.timeInMillis
                        ))
                    }
                }

                parseProjectDate(project.deadline)?.let { deadlineDate ->
                    if (!deadlineDate.time.before(today)) {
                        add(ActivityItem(
                            project.deadline,
                            "Deadline",
                            project,
                            deadlineDate.timeInMillis
                        ))
                    }
                }
            }
        }
        .sortedWith(compareBy<ActivityItem> { it.sortTime }.thenBy { it.type })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Scheduled Activities",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Upcoming shoots and project deadlines.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (activities.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No upcoming scheduled activities.",
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            activities.forEach { activity ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { onProjectClick(activity.project.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = activity.type,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activity.date,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = activity.project.projectName)
                        Text(text = "Location: ${activity.project.location}")
                        Text(text = "Status: ${activity.project.status}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onBackToScheduling,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Scheduling")
        }
    }
}

data class PaymentRecord(
    val amount: Double,
    val date: String
)

class FinanceStore(context: Context) {
    private val preferences = context.getSharedPreferences("creatorlog_finance", Context.MODE_PRIVATE)

    fun getProjectFee(projectId: Int): Double {
        return preferences.getFloat("fee_$projectId", 0f).toDouble()
    }

    fun setProjectFee(projectId: Int, fee: Double) {
        preferences.edit()
            .putFloat("fee_$projectId", fee.toFloat())
            .apply()
    }

    fun getPayments(projectId: Int): List<PaymentRecord> {
        val raw = preferences.getString("payments_$projectId", null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        PaymentRecord(
                            amount = item.optDouble("amount", 0.0),
                            date = item.optString("date", "")
                        )
                    )
                }
            }.reversed()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addPayment(projectId: Int, amount: Double) {
        val existing = getPayments(projectId).reversed().toMutableList()
        val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        existing.add(PaymentRecord(amount, date))

        val array = JSONArray()
        existing.forEach { payment ->
            array.put(
                JSONObject().apply {
                    put("amount", payment.amount)
                    put("date", payment.date)
                }
            )
        }

        preferences.edit()
            .putString("payments_$projectId", array.toString())
            .apply()
    }
}

fun formatCurrency(amount: Double): String {
    return String.format(Locale.getDefault(), "₱%,.2f", amount)
}


data class ProjectTask(
    val id: Long,
    val title: String,
    val category: String,
    val completed: Boolean
)

class TaskStore(context: Context) {
    private val preferences =
        context.getSharedPreferences("creatorlog_tasks", Context.MODE_PRIVATE)

    fun getTasks(projectId: Int): List<ProjectTask> {
        val raw = preferences.getString("tasks_$projectId", null) ?: return emptyList()

        return try {
            val array = JSONArray(raw)

            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)

                    add(
                        ProjectTask(
                            id = item.optLong("id"),
                            title = item.optString("title", ""),
                            category = item.optString("category", "Prep Tasks"),
                            completed = item.optBoolean("completed", false)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addTask(projectId: Int, title: String, category: String) {
        val tasks = getTasks(projectId).toMutableList()

        tasks.add(
            ProjectTask(
                id = System.currentTimeMillis(),
                title = title,
                category = category,
                completed = false
            )
        )

        saveTasks(projectId, tasks)
    }

    fun updateTask(projectId: Int, updatedTask: ProjectTask) {
        val tasks = getTasks(projectId).map { task ->
            if (task.id == updatedTask.id) updatedTask else task
        }

        saveTasks(projectId, tasks)
    }

    private fun saveTasks(projectId: Int, tasks: List<ProjectTask>) {
        val array = JSONArray()

        tasks.forEach { task ->
            array.put(
                JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("category", task.category)
                    put("completed", task.completed)
                }
            )
        }

        preferences.edit()
            .putString("tasks_$projectId", array.toString())
            .apply()
    }
}

private val taskCategories = listOf(
    "Prep Tasks",
    "Shooting Tasks",
    "Editing / Review Tasks",
    "Delivery Tasks"
)

@Composable
fun ProjectChecklistScreen(
    project: Project,
    onBackToProjectDetails: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tasks by remember(project.id) {
        mutableStateOf<List<ProjectTask>>(emptyList())
    }

    var taskApiError by remember(project.id) { mutableStateOf("") }
    var deletingTaskId by remember(project.id) { mutableStateOf<Int?>(null) }
    var showTaskDeletedDialog by remember(project.id) { mutableStateOf(false) }

    LaunchedEffect(project.id) {
        val token = TokenManager.getToken(context)
        if (!token.isNullOrBlank()) {
            try {
                val response = TaskApi.getTasks(token, project.id)
                if (response.success) {
                    tasks = response.tasks?.map {
                        ProjectTask(
                            id = it.id.toLong(),
                            title = it.title,
                            category = it.category ?: "Prep Tasks",
                            completed = it.status.equals("Completed", ignoreCase = true)
                        )
                    } ?: emptyList()
                } else {
                    taskApiError = response.message
                }
            } catch (e: Exception) {
                taskApiError = "Unable to load checklist from the server."
            }
        }
    }

    var selectedCategory by remember {
        mutableStateOf("Prep Tasks")
    }

    var showAddTaskDialog by remember {
        mutableStateOf(false)
    }

    var taskTitle by remember {
        mutableStateOf("")
    }

    var taskError by remember {
        mutableStateOf("")
    }

    val completedCount = tasks.count { it.completed }
    val totalCount = tasks.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Project Checklist",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = project.projectName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Checklist Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$completedCount of $totalCount tasks completed"
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (totalCount > 0) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = {
                            completedCount.toFloat() / totalCount.toFloat()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "No tasks added yet.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Task Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        taskCategories.forEach { category ->
            val categoryTasks = tasks.filter { it.category == category }
            val categoryCompleted = categoryTasks.count { it.completed }

            OutlinedButton(
                onClick = {
                    selectedCategory = category
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$category ($categoryCompleted/${categoryTasks.size})",
                    fontWeight = if (selectedCategory == category) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedCategory,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                val visibleTasks = tasks.filter {
                    it.category == selectedCategory
                }

                if (visibleTasks.isEmpty()) {
                    Text(
                        text = "No tasks in this category.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    visibleTasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.completed,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        val token = TokenManager.getToken(context)
                                        if (token.isNullOrBlank()) {
                                            taskApiError = "Your session has expired. Please login again."
                                        } else {
                                            try {
                                                val response = TaskApi.updateStatus(
                                                    token = token,
                                                    projectId = project.id,
                                                    taskId = task.id.toInt(),
                                                    status = if (checked) "Completed" else "Pending"
                                                )

                                                if (response.success) {
                                                    val refreshed = TaskApi.getTasks(token, project.id)
                                                    tasks = refreshed.tasks?.map {
                                                        ProjectTask(
                                                            id = it.id.toLong(),
                                                            title = it.title,
                                                            category = it.category ?: "Prep Tasks",
                                                            completed = it.status.equals("Completed", ignoreCase = true)
                                                        )
                                                    } ?: emptyList()
                                                } else {
                                                    taskApiError = response.message
                                                }
                                            } catch (e: Exception) {
                                                taskApiError = "Unable to update task status."
                                            }
                                        }
                                    }
                                }
                            )

                            Text(
                                text = task.title,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    deletingTaskId = task.id.toInt()
                                }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        taskTitle = ""
                        taskError = ""
                        showAddTaskDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Task")
                }
            }
        }

                    Button(
            onClick = onBackToProjectDetails,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Project Details")
        }
    }

    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTaskDialog = false
            },
            title = {
                Text(
                    "Add Task",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Category: $selectedCategory",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = {
                            taskTitle = it
                            taskError = ""
                        },
                        label = {
                            Text("Task Name")
                        },
                        singleLine = true,
                        isError = taskError.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (taskError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = taskError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddTaskDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanTitle = taskTitle.trim()

                        if (cleanTitle.isEmpty()) {
                            taskError = "Task name is required."
                        } else {
                            scope.launch {
                                val token = TokenManager.getToken(context)
                                if (token.isNullOrBlank()) {
                                    taskApiError = "Your session has expired. Please login again."
                                } else {
                                    try {
                                        val response = TaskApi.createTask(
                                            token = token,
                                            projectId = project.id,
                                            title = cleanTitle,
                                            category = selectedCategory
                                        )

                                        if (response.success) {
                                            val refreshed = TaskApi.getTasks(token, project.id)
                                            tasks = refreshed.tasks?.map {
                                                ProjectTask(
                                                    id = it.id.toLong(),
                                                    title = it.title,
                                                    category = it.category ?: "Prep Tasks",
                                                    completed = it.status.equals("Completed", ignoreCase = true)
                                                )
                                            } ?: emptyList()

                                            showAddTaskDialog = false
                                        } else {
                                            taskApiError = response.message
                                        }
                                    } catch (e: Exception) {
                                        taskApiError = "Unable to add task."
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        )
    }

    val taskToDelete = tasks.find { it.id.toInt() == deletingTaskId }

    if (deletingTaskId != null && taskToDelete != null && !showTaskDeletedDialog) {
        AlertDialog(
            onDismissRequest = {
                deletingTaskId = null
            },
            title = {
                Text(
                    text = "Delete Task?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${taskToDelete.title}\"?\n\nThis action cannot be undone."
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deletingTaskId = null
                    }
                ) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val token = TokenManager.getToken(context)

                            if (token.isNullOrBlank()) {
                                deletingTaskId = null
                                taskApiError = "Your session has expired. Please login again."
                            } else {
                                try {
                                    val response = TaskApi.deleteTask(
                                        token = token,
                                        projectId = project.id,
                                        taskId = taskToDelete.id.toInt()
                                    )

                                    if (response.success) {
                                        val refreshed = TaskApi.getTasks(token, project.id)
                                        tasks = refreshed.tasks?.map {
                                            ProjectTask(
                                                id = it.id.toLong(),
                                                title = it.title,
                                                category = it.category ?: "Prep Tasks",
                                                completed = it.status.equals("Completed", ignoreCase = true)
                                            )
                                        } ?: emptyList()

                                        deletingTaskId = null
                                        showTaskDeletedDialog = true
                                    } else {
                                        deletingTaskId = null
                                        taskApiError = response.message
                                    }
                                } catch (e: Exception) {
                                    deletingTaskId = null
                                    taskApiError = "Unable to delete task."
                                }
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            }
        )
    }

    if (showTaskDeletedDialog) {
        LaunchedEffect(Unit) {
            delay(1500)
            showTaskDeletedDialog = false
        }

        AlertDialog(
            onDismissRequest = {
                showTaskDeletedDialog = false
            },
            title = {
                Text(
                    text = "✓  Task Deleted",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("The task has been deleted successfully.")
            },
            confirmButton = {}
        )
    }

    if (taskApiError.isNotBlank()) {
        AlertDialog(
            onDismissRequest = {
                taskApiError = ""
            },
            title = {
                Text("Task Error", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(taskApiError)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskApiError = ""
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}


data class ReminderProject(
    val project: Project,
    val daysUntil: Long
)

private fun parseCreatorLogDate(value: String): Calendar? {
    return try {
        val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        formatter.isLenient = false
        val date = formatter.parse(value) ?: return null
        Calendar.getInstance().apply { time = date }
    } catch (_: Exception) {
        null
    }
}

private fun daysFromToday(dateText: String): Long? {
    val target = parseCreatorLogDate(dateText) ?: return null
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)
    target.set(Calendar.HOUR_OF_DAY, 0)
    target.set(Calendar.MINUTE, 0)
    target.set(Calendar.SECOND, 0)
    target.set(Calendar.MILLISECOND, 0)
    return ((target.timeInMillis - today.timeInMillis) / (24L * 60L * 60L * 1000L))
}

@Composable
private fun ReminderProjectCard(
    title: String,
    project: Project,
    dateLabel: String,
    detail: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(project.projectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$dateLabel: ${if (dateLabel == "Deadline") project.deadline else project.eventDate}")
            Text(detail)
            Text("Status: ${project.status}")
        }
    }
}

@Composable
fun DeadlineRemindersScreen(
    projects: List<Project>,
    onProjectClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val reminders = projects.mapNotNull { project ->
        daysFromToday(project.deadline)?.let { days -> ReminderProject(project, days) }
    }.filter { it.daysUntil <= 7L }.sortedBy { it.daysUntil }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Deadline Reminders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Projects due within 7 days or already overdue.")
        Spacer(modifier = Modifier.height(20.dp))
        if (reminders.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) { Text("No deadline reminders right now.", modifier = Modifier.padding(18.dp)) }
        } else {
            reminders.forEach { item ->
                val detail = when {
                    item.daysUntil < 0 -> "Overdue by ${-item.daysUntil} day(s)"
                    item.daysUntil == 0L -> "Deadline is today"
                    item.daysUntil == 1L -> "Deadline is tomorrow"
                    else -> "${item.daysUntil} day(s) remaining"
                }
                ReminderProjectCard("Deadline Reminder", item.project, "Deadline", detail) { onProjectClick(item.project.id) }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Dashboard") }
    }
}

@Composable
fun ShootRemindersScreen(
    projects: List<Project>,
    onProjectClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val reminders = projects.mapNotNull { project ->
        daysFromToday(project.eventDate)?.let { days -> ReminderProject(project, days) }
    }.filter { it.daysUntil in 0L..7L }.sortedBy { it.daysUntil }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Shoot Reminders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Upcoming shoots within the next 7 days.")
        Spacer(modifier = Modifier.height(20.dp))
        if (reminders.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) { Text("No upcoming shoot reminders right now.", modifier = Modifier.padding(18.dp)) }
        } else {
            reminders.forEach { item ->
                val detail = when (item.daysUntil) {
                    0L -> "Shoot is today"
                    1L -> "Shoot is tomorrow"
                    else -> "Shoot in ${item.daysUntil} day(s)"
                }
                ReminderProjectCard("Shoot Reminder", item.project, "Event Date", detail) { onProjectClick(item.project.id) }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Dashboard") }
    }
}

@Composable
fun PendingTaskRemindersScreen(
    projects: List<Project>,
    onProjectClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val taskStore = remember { TaskStore(context) }
    val pending = projects.mapNotNull { project ->
        val tasks = taskStore.getTasks(project.id)
        val incomplete = tasks.count { !it.completed }
        if (incomplete > 0) project to incomplete else null
    }.sortedByDescending { it.second }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Pending Task Reminders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Projects that still have unfinished checklist tasks.")
        Spacer(modifier = Modifier.height(20.dp))
        if (pending.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) { Text("No pending tasks. Great job!", modifier = Modifier.padding(18.dp)) }
        } else {
            pending.forEach { (project, count) ->
                ReminderProjectCard("Pending Task Reminder", project, "Deadline", "$count task(s) still pending") { onProjectClick(project.id) }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Dashboard") }
    }
}

@Composable
fun ProjectHistoryScreen(
    projects: List<Project>,
    clients: List<Client>,
    onProjectClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val history = projects.sortedWith(compareBy<Project> { parseCreatorLogDate(it.eventDate)?.timeInMillis ?: Long.MAX_VALUE }.thenBy { it.projectName })

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Project History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("A record of your projects, clients, dates, and current status.")
        Spacer(modifier = Modifier.height(20.dp))
        if (history.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) { Text("No project history yet.", modifier = Modifier.padding(18.dp)) }
        } else {
            history.forEach { project ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onProjectClick(project.id) }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(project.projectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Client: ${clients.find { it.id == project.clientId }?.fullName ?: "Unknown client"}")
                        Text("Event Date: ${project.eventDate}")
                        Text("Deadline: ${project.deadline}")
                        Text("Status: ${project.status}")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Dashboard") }
    }
}

@Composable
fun ProjectSummaryDashboardScreen(
    projects: List<Project>,
    clients: List<Client>,
    onProjectClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val financeStore = remember { FinanceStore(context) }
    val taskStore = remember { TaskStore(context) }

    val totalFees = projects.sumOf { financeStore.getProjectFee(it.id) }
    val totalPaid = projects.sumOf { financeStore.getPayments(it.id).sumOf { payment -> payment.amount } }
    val totalBalance = (totalFees - totalPaid).coerceAtLeast(0.0)
    val completedProjects = projects.count { it.status.equals("Delivered", ignoreCase = true) }
    val pendingTasks = projects.sumOf { taskStore.getTasks(it.id).count { task -> !task.completed } }
    val upcomingShoots = projects.count { daysFromToday(it.eventDate)?.let { days -> days in 0L..7L } == true }
    val overdueDeadlines = projects.count { daysFromToday(it.deadline)?.let { days -> days < 0L } == true }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Project Summary Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Overall performance, scheduling, tasks, and finance summary.")
        Spacer(modifier = Modifier.height(20.dp))

        SummaryMetricCard("Total Projects", projects.size.toString())
        SummaryMetricCard("Delivered Projects", completedProjects.toString())
        SummaryMetricCard("Upcoming Shoots (7 days)", upcomingShoots.toString())
        SummaryMetricCard("Pending Tasks", pendingTasks.toString())
        SummaryMetricCard("Overdue Deadlines", overdueDeadlines.toString())
        SummaryMetricCard("Total Project Fees", formatCurrency(totalFees))
        SummaryMetricCard("Total Payments", formatCurrency(totalPaid))
        SummaryMetricCard("Outstanding Balance", formatCurrency(totalBalance))

        Spacer(modifier = Modifier.height(20.dp))
        Text("Projects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        projects.forEach { project ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onProjectClick(project.id) }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(project.projectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${project.projectType} • ${project.status}")
                    Text("Deadline: ${project.deadline}")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Dashboard") }
    }
}

@Composable
private fun SummaryMetricCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NotificationsScreen(
    refreshKey: Int,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var notifications by remember(refreshKey) {
        mutableStateOf<List<NotificationApiItem>>(emptyList())
    }
    var errorMessage by remember(refreshKey) { mutableStateOf("") }
    var isLoading by remember(refreshKey) { mutableStateOf(true) }

    fun reload(scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            val token = TokenManager.getToken(context)
            if (token.isNullOrBlank()) {
                errorMessage = "Your session has expired. Please login again."
                isLoading = false
                return@launch
            }

            try {
                val response = NotificationApi.getNotifications(token)
                if (response.success) {
                    notifications = response.notifications ?: emptyList()
                    errorMessage = ""
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Unable to load notifications."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(refreshKey) {
        reload(scope)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text("Your account notifications and reminders.")

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Text("Loading notifications...")
        } else if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        } else if (notifications.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No notifications yet.",
                    modifier = Modifier.padding(18.dp)
                )
            }
        } else {
            notifications.forEach { notification ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(notification.message)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Type: ${notification.type ?: "General"}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = if (notification.is_read) "Read" else "Unread",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!notification.is_read) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val token = TokenManager.getToken(context)
                                            if (!token.isNullOrBlank()) {
                                                try {
                                                    val response = NotificationApi.markRead(
                                                        token,
                                                        notification.id
                                                    )
                                                    if (response.success) {
                                                        reload(scope)
                                                    } else {
                                                        errorMessage = response.message
                                                    }
                                                } catch (_: Exception) {
                                                    errorMessage = "Unable to mark notification as read."
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Mark Read")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val token = TokenManager.getToken(context)
                                        if (!token.isNullOrBlank()) {
                                            try {
                                                val response = NotificationApi.delete(
                                                    token,
                                                    notification.id
                                                )
                                                if (response.success) {
                                                    reload(scope)
                                                } else {
                                                    errorMessage = response.message
                                                }
                                            } catch (_: Exception) {
                                                errorMessage = "Unable to delete notification."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}
