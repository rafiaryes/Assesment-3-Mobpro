package com.rafiarya0114.katalogmotor.ui.screen

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.rafiarya0114.katalogmotor.BuildConfig
import com.rafiarya0114.katalogmotor.R
import com.rafiarya0114.katalogmotor.model.Katalog
import com.rafiarya0114.katalogmotor.model.User
import com.rafiarya0114.katalogmotor.network.ApiStatus
import com.rafiarya0114.katalogmotor.network.KatalogApi
import com.rafiarya0114.katalogmotor.network.UserDataStore
import com.rafiarya0114.katalogmotor.ui.theme.AboutMeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User("", "", ""))
    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showKatalogDialog by remember { mutableStateOf(false) }

    val deleteStatus by viewModel.deleteStatus
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedKatalog by remember { mutableStateOf<Katalog?>(null) }
    LaunchedEffect(deleteStatus) {
        if (deleteStatus != null) {
            Toast.makeText(context, deleteStatus, Toast.LENGTH_SHORT).show()
            viewModel.clearDeleteStatus()
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.token.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                signIn(viewModel, context, dataStore)
                            }
                        } else {
//                            Log.d("SIGN-IN", "User: $user")
                            showDialog = true
                        }
                    })
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle_24),
                            contentDescription = stringResource(id = R.string.profil),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showKatalogDialog = true
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.tambah_hewan)
                )
            }
        }
    ){ innerPadding ->
        ScreenContent(
            viewModel,
            token = user.token,
            modifier = Modifier.padding(innerPadding),
            onDeleteClick = { katalog ->
                selectedKatalog = katalog
                showDeleteDialog = true
            }
        )
        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    signOut(context, dataStore)
                }
                showDialog = false
            }
        }
        if (showKatalogDialog) {
            KatalogDialog(
                katalog = null,
                onDismissRequest = { showKatalogDialog = false }) { judul, manufacturer, harga, bitmap ->
                viewModel.saveData(user.token, judul, manufacturer, harga, bitmap)
                showKatalogDialog = false
            }
        }
        if (showDeleteDialog && selectedKatalog != null) {
            DisplayAlertDialog(
                katalog = selectedKatalog!!,
                onDismiss = {
                    showDeleteDialog = false
                    selectedKatalog = null
                },
                onConfirm = {
                    viewModel.deleteData(user.token, selectedKatalog!!.id_katalog)
                    showDeleteDialog = false
                    selectedKatalog = null
                }
            )
        }
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(viewModel: MainViewModel, token: String, onDeleteClick: (Katalog) -> Unit, modifier: Modifier = Modifier) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()
    var showDetailDialog by remember { mutableStateOf<Katalog?>(null) }

    LaunchedEffect(token) {
        viewModel.retrieveData(token)
    }

    if (showDetailDialog != null) {
        KatalogDialog(
            katalog = showDetailDialog!!,
            onDismissRequest = { showDetailDialog = null },
            onConfirmation = { judul, manufacturer, harga, bitmap ->
                viewModel.updateData(token, showDetailDialog!!.id_katalog, judul, manufacturer, harga, bitmap)
                showDetailDialog = null
            }
        )
    }

    when(status){
        ApiStatus.LOADING -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize().padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(data) {
                    ListItem(
                        katalog = it,
                        onDeleteClick = if (it.mine == 1) { // Hanya untuk hewan milik user
                            { onDeleteClick(it) }
                        } else null
                    ) {
                        showDetailDialog = it
                    }
                }
            }
        }
        ApiStatus.FAILED ->{
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = {viewModel.retrieveData(token)},
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }

            }
        }
    }
}

private suspend fun signIn(viewModel: MainViewModel, context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(viewModel, result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(viewModel: MainViewModel, result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(
                User(
                    name = nama,
                    email = email,
                    photoUrl = photoUrl
                )
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User("", "", ""))
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

@Composable
fun ListItem(
    katalog: Katalog,
    onDeleteClick: (() -> Unit)? = null,
    onUpdateClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .clickable {
                if (onUpdateClick != null) {
                    onUpdateClick()
                }
            }
            .padding(4.dp)
            .border(1.dp, Color.Gray),
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    KatalogApi.getImageUrl(katalog.id_katalog)
                )
                .crossfade(enable = true)
                .build(),
            contentDescription = stringResource(R.string.gambar, katalog.judul),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.baseline_broken_image_24),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = katalog.judul,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = katalog.manufacturer,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            if (onDeleteClick != null) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.hapus),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    AboutMeTheme {
        MainScreen()
        }
}