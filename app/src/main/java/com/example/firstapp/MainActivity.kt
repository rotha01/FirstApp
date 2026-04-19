package com.example.firstapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstapp.ui.theme.FirstAppTheme
import java.util.UUID

// ==========================================
// 1. DATA MODEL & ENUMS
// ==========================================
data class Employee(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val position: String,
    val gender: String,
    val salary: Double,
    val phone: String,
    val address: String
)

enum class Screen { DASHBOARD, ADD_EMPLOYEE, MANAGE, VIEW_SEARCH }

// ==========================================
// 2. THEME COLORS
// ==========================================
val Blue_Primary = Color(0xFF1976D2)
val Blue_Surface = Color(0xFFE3F2FD)
val Blue_Accent = Color(0xFFBBDEFB)
val Blue_OnPrimary = Color.White
val Neutral_Light = Color(0xFFF5F7FA)

// ==========================================
// 3. VIEWMODEL (Updated with Position Management)
// ==========================================
class EmployeeViewModel : ViewModel() {
    var employees by mutableStateOf(listOf<Employee>())
        private set

    //  Dynamic list of positions
    var positions by mutableStateOf(listOf("Manager", "Developer", "Designer", "HR", "Sales", "Intern"))
        private set

    fun addEmployee(emp: Employee) { employees = employees + emp }
    fun deleteEmployee(emp: Employee) { employees = employees.filter { it.id != emp.id } }
    fun updateEmployee(updated: Employee) {
        employees = employees.map { if (it.id == updated.id) updated else it }
    }

    //  Add new position logic
    fun addPosition(newPos: String) {
        if (newPos.isNotBlank() && !positions.contains(newPos)) {
            positions = (positions + newPos).sorted()
        }
    }
}

// ==========================================
// 4. MAIN ACTIVITY & NAVIGATION
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme(darkTheme = false) { MyApp() }
        }
    }
}

@Composable
fun MyApp(viewModel: EmployeeViewModel = viewModel()) {
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var currentScreen by rememberSaveable { mutableStateOf(Screen.DASHBOARD) }

    if (!isLoggedIn) {
        ModernLoginScreen { isLoggedIn = true }
    } else {
        when (currentScreen) {
            Screen.DASHBOARD -> ModernDashboardScreen(
                employees = viewModel.employees,
                onAddEmployee = { currentScreen = Screen.ADD_EMPLOYEE },
                onManage = { currentScreen = Screen.MANAGE },
                onViewSearch = { currentScreen = Screen.VIEW_SEARCH },
                onLogout = { isLoggedIn = false }
            )
            Screen.ADD_EMPLOYEE -> ModernAddEmployeeScreen(
                viewModel = viewModel, //  Pass VM to handle positions
                onBack = { currentScreen = Screen.DASHBOARD },
                onSave = { viewModel.addEmployee(it); currentScreen = Screen.DASHBOARD }
            )
            Screen.MANAGE -> ModernManageScreen(
                employees = viewModel.employees,
                positions = viewModel.positions,
                onBack = { currentScreen = Screen.DASHBOARD },
                onDelete = { viewModel.deleteEmployee(it) },
                onUpdate = { viewModel.updateEmployee(it) }
            )
            Screen.VIEW_SEARCH -> ModernViewScreen(
                employees = viewModel.employees,
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }
    }
}

// ==========================================
// 5. LOGIN SCREEN
// ==========================================
@Composable
fun ModernLoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(Modifier.fillMaxSize().background(Neutral_Light), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(24.dp).clip(RoundedCornerShape(28.dp)).background(Color.White).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Lock, null, tint = Blue_Primary, modifier = Modifier.size(64.dp))
            Text("Admin Login", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (email == "admin@email.com" && password == "1234") onLoginSuccess()
                    else Toast.makeText(context, "Invalid Login", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue_Primary)
            ) {
                Text("Sign In", color = Color.White)
            }
        }
    }
}

// ==========================================
// 6. DASHBOARD SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboardScreen(
    employees: List<Employee>,
    onAddEmployee: () -> Unit,
    onManage: () -> Unit,
    onViewSearch: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employee Manager", color = Blue_OnPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue_Primary)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Blue_Primary) {
                NavigationBarItem(false, onAddEmployee, { Icon(Icons.Default.PersonAdd, null, tint = Blue_OnPrimary) }, label = { Text("Add", color = Blue_OnPrimary) })
                NavigationBarItem(false, onManage, { Icon(Icons.Default.Settings, null, tint = Blue_OnPrimary) }, label = { Text("Manage", color = Blue_OnPrimary) })
                NavigationBarItem(false, onViewSearch, { Icon(Icons.Default.Search, null, tint = Blue_OnPrimary) }, label = { Text("Search", color = Blue_OnPrimary) })
                NavigationBarItem(false, onLogout, { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Blue_OnPrimary) }, label = { Text("Logout", color = Blue_OnPrimary) })
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HeaderSection(employees.size)
            // Inside ModernDashboardScreen column
            GenderChartCard(
                male = employees.count { it.gender == "Male" },
                female = employees.count { it.gender == "Female" },
                other = employees.count { it.gender == "Other" } //  Added this
            )
            Text("Quick View", fontWeight = FontWeight.Bold)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(employees.takeLast(5)) { emp -> ModernEmployeeCard(emp) }
            }
        }
    }
}

// ==========================================
// 7. VIEW & SEARCH SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernViewScreen(employees: List<Employee>, onBack: () -> Unit) {
    var search by remember { mutableStateOf("") }
    val filtered = employees.filter { it.name.contains(search, true) || it.position.contains(search, true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Directory", color = Blue_OnPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Blue_OnPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue_Primary)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            ModernSearchBar(search, { search = it })
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { emp -> DetailedEmployeeCard(emp) }
            }
        }
    }
}

@Composable
fun DetailedEmployeeCard(emp: Employee) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(emp.position, color = Blue_Primary)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("📞 ${emp.phone}", style = MaterialTheme.typography.bodyMedium)
            Text("📍 ${emp.address}", style = MaterialTheme.typography.bodyMedium)
            Text("💰 Salary: $${emp.salary}", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 8. ADD EMPLOYEE SCREEN (Updated with Position Manager)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAddEmployeeScreen(viewModel: EmployeeViewModel, onBack: () -> Unit, onSave: (Employee) -> Unit) {
    var name by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf(viewModel.positions.getOrElse(0) { "Staff" }) }
    var gen by remember { mutableStateOf("Male") }
    var sal by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var addr by remember { mutableStateOf("") }

    //  Add Context to show Toast messages
    val context = LocalContext.current
    var showPosDialog by remember { mutableStateOf(false) }

    if (showPosDialog) {
        AddPositionDialog(
            onDismiss = { showPosDialog = false },
            onConfirm = {
                viewModel.addPosition(it)
                pos = it
                showPosDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Staff", color = Blue_OnPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Blue_OnPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue_Primary)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModernInputField(name, { name = it }, "Full Name", Icons.Default.Person)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ModernDropdown("Position", viewModel.positions, pos, { pos = it }, Icons.Default.Work)
                }
                IconButton(
                    onClick = { showPosDialog = true },
                    modifier = Modifier.background(Blue_Surface, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Position", tint = Blue_Primary)
                }
            }

            ModernDropdown("Gender", listOf("Male", "Female", "Other"), gen, { gen = it }, Icons.Default.Transgender)
            ModernInputField(ph, { ph = it }, "Phone Number", Icons.Default.Phone)
            ModernInputField(addr, { addr = it }, "Home Address", Icons.Default.LocationOn)
            ModernInputField(sal, { sal = it }, "Salary ($)", Icons.Default.Payments)

            Button(
                onClick = {
                    //  VALIDATION LOGIC
                    when {
                        name.isBlank() -> Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
                        ph.isBlank() -> Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                        addr.isBlank() -> Toast.makeText(context, "Please enter an address", Toast.LENGTH_SHORT).show()
                        sal.isBlank() -> Toast.makeText(context, "Please enter the salary", Toast.LENGTH_SHORT).show()
                        else -> {
                            //  All clear - Save the employee
                            onSave(
                                Employee(
                                    name = name,
                                    position = pos,
                                    gender = gen,
                                    salary = sal.toDoubleOrNull() ?: 0.0,
                                    phone = ph,
                                    address = addr
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue_Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Employee")
            }
        }
    }
}

// New Dialog for adding Positions
@Composable
fun AddPositionDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newPos by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Position") },
        text = {
            OutlinedTextField(newPos, { newPos = it }, label = { Text("Position Name") }, singleLine = true)
        },
        confirmButton = {
            Button(onClick = { onConfirm(newPos) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ==========================================
// 9. MANAGE SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernManageScreen(employees: List<Employee>, positions: List<String>,onBack: () -> Unit, onDelete: (Employee) -> Unit, onUpdate: (Employee) -> Unit) {
    var search by remember { mutableStateOf("") }
    var empToDelete by remember { mutableStateOf<Employee?>(null) }

    val filtered = employees.filter {
        it.name.contains(search, ignoreCase = true) || it.position.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Staff", color = Blue_OnPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Blue_OnPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue_Primary)
            )
        }
    ) { padding ->
        if (empToDelete != null) {
            AlertDialog(
                onDismissRequest = { empToDelete = null },
                title = { Text("Confirm Deletion") },
                text = { Text("Delete ${empToDelete?.name}?") },
                confirmButton = { Button(onClick = { onDelete(empToDelete!!); empToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") } },
                dismissButton = { TextButton(onClick = { empToDelete = null }) { Text("Cancel") } }
            )
        }

        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ModernSearchBar(search, { search = it })
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { emp ->
                    EditableEmployeeCard(
                        emp = emp,
                        positions = positions,
                        onUpdate = onUpdate,
                        onDeleteRequest = { empToDelete = emp }
                    )
                }
            }
        }
    }
}

@Composable
fun EditableEmployeeCard(
    emp: Employee,
    positions: List<String>, //  Pass the dynamic positions list
    onUpdate: (Employee) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }

    //  Use remember(emp) to ensure state resets correctly when clicking different staff
    var salary by remember(emp) { mutableStateOf(emp.salary.toString()) }
    var selectedPos by remember(emp) { mutableStateOf(emp.position) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = { editing = !editing }) {
                        Icon(Icons.Default.Edit, null, tint = Blue_Primary)
                    }
                    IconButton(onClick = onDeleteRequest) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            }

            if (editing) {
                //  Update Position Dropdown
                ModernDropdown(
                    label = "Update Position",
                    options = positions,
                    selected = selectedPos,
                    onSelect = { selectedPos = it },
                    icon = Icons.Default.Work
                )

                // Update Salary Input
                ModernInputField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = "Update Salary",
                    icon = Icons.Default.AttachMoney
                )

                Button(
                    onClick = {
                        val newSal = salary.toDoubleOrNull() ?: emp.salary
                        //  Save both new Position and Salary
                        onUpdate(emp.copy(position = selectedPos, salary = newSal))
                        editing = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue_Primary)
                ) {
                    Text("Save Changes")
                }
            } else {

                Text(emp.position, color = Blue_Primary, fontWeight = FontWeight.Medium)
                Text("Salary: $${emp.salary}", color = Color.Gray)
            }
        }
    }
}

// ==========================================
// 10. REUSABLE UI HELPERS
// ==========================================
@Composable
fun ModernSearchBar(search: String, onSearchChanged: (String) -> Unit) {
    OutlinedTextField(
        value = search,
        onValueChange = onSearchChanged,
        label = { Text("Search staff...") },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Blue_Primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit, icon: ImageVector) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected, onValueChange = {}, readOnly = true, label = { Text(label) },
            leadingIcon = { Icon(icon, null, tint = Blue_Primary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false }) }
        }
    }
}

@Composable
fun HeaderSection(total: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("Welcome Admin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Manage your team efficiently", style = MaterialTheme.typography.bodySmall)
        }
        Card(colors = CardDefaults.cardColors(containerColor = Blue_Accent)) {
            Text("Total: $total", Modifier.padding(8.dp), fontWeight = FontWeight.Bold, color = Blue_Primary)
        }
    }
}

@Composable
fun GenderChartCard(male: Int, female: Int, other: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Staff Gender Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            val total = (male + female + other).coerceAtLeast(1)

            //  Custom Multi-Segment Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE)) // Background for empty state
            ) {
                if (male > 0) Box(Modifier.weight(male.toFloat()).fillMaxHeight().background(Blue_Primary))
                if (female > 0) Box(Modifier.weight(female.toFloat()).fillMaxHeight().background(Color(0xFFE91E63))) // Pink for Female
                if (other > 0) Box(Modifier.weight(other.toFloat()).fillMaxHeight().background(Color(0xFF9C27B0))) // Purple for Other
            }

            Spacer(modifier = Modifier.height(12.dp))

            //  Legend / Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GenderLabel("Male", male, Blue_Primary)
                GenderLabel("Female", female, Color(0xFFE91E63))
                GenderLabel("Other", other, Color(0xFF9C27B0))
            }
        }
    }
}

@Composable
fun GenderLabel(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(count.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ModernEmployeeCard(emp: Employee) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Blue_Surface), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = Blue_Primary)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(emp.name, fontWeight = FontWeight.Bold)
                Text(emp.position, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ModernInputField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, leadingIcon = { Icon(icon, null, tint = Blue_Primary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
}