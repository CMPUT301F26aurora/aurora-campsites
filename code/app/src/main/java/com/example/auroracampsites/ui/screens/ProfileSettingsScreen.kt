package com.example.auroracampsites.ui.screens

/*
- User Stories:
- US 01.07.03 - Update profile information.
- US 01.08.01 - Delete user profile.
 *
- Description:
- Allows users to update their profile picture,
- name, email and phone number, or confirm
- deletion of their profile.
 */

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.auroracampsites.data.models.User
import com.example.auroracampsites.data.models.UserRole
import com.example.auroracampsites.data.repository.UserRepository

/**
 * Represents structured form data for user profile settings modifications,
 * including name, email address, and phone number fields.
 *
 * @property name The current profile username.
 * @property email The current user contact email.
 * @property phoneNumber The current contact phone number.
 */
data class ProfileSettingsData(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = ""
)

/**
 * Manages the data state and screen switch for user profile setting screen,
 * including updating profile fields and facilitating profile deletion workflows.
 */
@Composable
fun ProfileManagementScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    currentUser: User = User(
        id = "Admin1",
        username = "admin_user",
        email = "placeholder@email.com",
        phoneNumber = "582-110-2224",
        role = UserRole.ADMIN,
        notificationsEnabled = true
    )
) {
    val userRepository = remember { UserRepository() }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showOptOutDialog by remember { mutableStateOf(false) }

    if (showOptOutDialog) {
        AlertDialog(
            onDismissRequest = { showOptOutDialog = false },
            title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
            text = { Text("You have opted out of notifications!") },
            confirmButton = {
                Button(
                    onClick = { showOptOutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        DeleteProfileScreen(
            modifier = modifier,
            onConfirmDelete = {
                userRepository.deleteUser(currentUser)
                navController.popBackStack()
            },
            onGoBack = {
                showDeleteConfirmation = false
            }
        )
    } else {
        ProfileSettingsScreen(
            navController = navController,
            modifier = modifier,
            profile = ProfileSettingsData(
                name = currentUser.username,
                email = currentUser.email,
                phoneNumber = currentUser.phoneNumber
            ),
            onUpdateName = { newName ->
                userRepository.updateUserUsername(currentUser, newName)
            },
            onUpdateEmail = { newEmail ->
                userRepository.updateUserEmail(currentUser, newEmail)
            },
            onUpdatePhone = { newPhone ->
                userRepository.updateUserPhoneNumber(currentUser, newPhone)
            },
            onBack = {
                navController.popBackStack()
            },
            onAppPolicy = {
                navController.navigate("app_policy")
            },
            onNotificationOptOut = {
                userRepository.updateUserNotifs(currentUser)
                showOptOutDialog = true
            },
            onDeleteClick = {
                showDeleteConfirmation = true
            }
        )
    }
}


@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    profile: ProfileSettingsData = ProfileSettingsData(),
    onUpdateName: (String) -> Unit = {},
    onUpdateEmail: (String) -> Unit = {},
    onUpdatePhone: (String) -> Unit = {},
    onUpdateImage: (Uri) -> Unit = {},
    onBack: () -> Unit = {},
    onAppPolicy: () -> Unit = {},
    onNotificationOptOut: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var currentName by remember(profile.name) { mutableStateOf(profile.name) }
    var currentEmail by remember(profile.email) { mutableStateOf(profile.email) }
    var currentPhone by remember(profile.phoneNumber) { mutableStateOf(profile.phoneNumber) }

    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri
            onUpdateImage(uri)
            message = "Image selected. Save integration pending."
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AURORA CAMPSITES",
                fontSize = 24.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, Color.Black),
                    contentPadding = PaddingValues(horizontal = 18.dp)
                ) {
                    Text(
                        text = "Back",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = "Profile Settings",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(25.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Profile Picture",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(Color(0xFFE6E6E6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedImage == null) "Image" else "Selected",
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            imagePicker.launch("image/*")
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(
                            "Upload Image...",
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1.1f)
                ) {
                    Text(
                        text = "Profile Info",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ProfileEditField(
                        label = "Current Name:",
                        currentValue = currentName,
                        placeholder = "Enter new name...",
                        value = newName,
                        onValueChange = { newName = it },
                        onUpdate = {
                            if (newName.isNotBlank()) {
                                onUpdateName(newName.trim())
                                currentName = newName.trim()
                                newName = ""
                                message = "Name updated in UI."
                            }
                        }
                    )

                    ProfileEditField(
                        label = "Current Email:",
                        currentValue = currentEmail,
                        placeholder = "Enter new email...",
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        keyboardType = KeyboardType.Email,
                        onUpdate = {
                            val email = newEmail.trim()
                            if (
                                email.contains("@") &&
                                email.substringAfter("@").contains(".")
                            ) {
                                onUpdateEmail(email)
                                currentEmail = email
                                newEmail = ""
                                message = "Email updated in UI."
                            } else {
                                message = "Please enter a valid email."
                            }
                        }
                    )

                    ProfileEditField(
                        label = "Current Phone Number:",
                        currentValue = currentPhone,
                        placeholder = "Enter new number...",
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        keyboardType = KeyboardType.Phone,
                        onUpdate = {
                            if (newPhone.isNotBlank()) {
                                onUpdatePhone(newPhone.trim())
                                currentPhone = newPhone.trim()
                                newPhone = ""
                                message = "Phone updated in UI."
                            }
                        }
                    )
                }
            }

            if (message.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(Modifier.weight(1f, fill = false))
            Spacer(Modifier.height(50.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileBottomButton(
                    text = "App Policy",
                    onClick = onAppPolicy
                )

                ProfileBottomButton(
                    text = "Notif Opt-Out",
                    onClick = onNotificationOptOut
                )

                Spacer(Modifier.weight(1f))

                ProfileBottomButton(
                    text = "Delete Profile",
                    onClick = onDeleteClick,
                    textColor = Color.Red
                )
            }
        }
    }
}

/**
 * Reusable input field component for editing specific profile attributes.
 */
@Composable
private fun ProfileEditField(
    label: String,
    currentValue: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    onUpdate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "$label\n$currentValue",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(5.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 11.sp
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape = RoundedCornerShape(50)
        )

        Spacer(Modifier.height(5.dp))

        OutlinedButton(
            onClick = onUpdate,
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.5.dp, Color.Black),
            contentPadding = PaddingValues(
                horizontal = 18.dp,
                vertical = 0.dp
            )
        ) {
            Text(
                text = "Update",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ProfileBottomButton(
    text: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, Color.Black),
        contentPadding = PaddingValues(
            horizontal = 9.dp,
            vertical = 0.dp
        )
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Displays a confirmation screen warning the user before permanent account deletion.
 *
 */
@Composable
fun DeleteProfileScreen(
    modifier: Modifier = Modifier,
    onConfirmDelete: () -> Unit = {},
    onGoBack: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AURORA CAMPSITES",
                fontSize = 24.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(Modifier.height(65.dp))

            Text(
                text = "Do you wish to\ndelete your\nprofile?",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "This action cannot be undone!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(60.dp))

            Button(
                onClick = onConfirmDelete,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6E6E9),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Yes, Delete my Profile",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onGoBack,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6E6E9),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "No, Go Back",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}