<template>
  <main :style="{'background-image': 'url(/09-12-2016_yuzhno-russkoe_2.jpg)', 'min-height': '100vh'}">
    <Navbar/>
    <div class="container py-4">
      <div class="row justify-content-center">
        <div class="col-12 mt-5">
          <h1 class="text-center mb-4 text-light">Административная панель</h1>

          <!-- Статистика -->
          <div class="row mb-4">
            <div class="col-md-3 mb-3">
              <div class="card bg-primary text-white h-100">
                <div class="card-body">
                  <h5 class="card-title"><i class="bi bi-people me-2"></i>Пользователи</h5>
                  <p class="card-text display-5">{{ users.length }}</p>
                </div>
              </div>
            </div>
            <div class="col-md-3 mb-3">
              <div class="card bg-success text-white h-100">
                <div class="card-body">
                  <h5 class="card-title"><i class="bi bi-journal-text me-2"></i>Журналы</h5>
                  <p class="card-text display-5">{{ stats.journals }}</p>
                </div>
              </div>
            </div>
            <div class="col-md-3 mb-3">
              <div class="card bg-info text-white h-100">
                <div class="card-body">
                  <h5 class="card-title"><i class="bi bi-file-earmark-pdf me-2"></i>PDF</h5>
                  <p class="card-text display-5">{{ stats.pdfs }}</p>
                </div>
              </div>
            </div>
            <div class="col-md-3 mb-3">
              <div class="card bg-warning text-dark h-100">
                <div class="card-body">
                  <h5 class="card-title"><i class="bi bi-exclamation-triangle me-2"></i>Ошибки</h5>
                  <p class="card-text display-5">{{ stats.errors }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Табы -->
          <ul class="nav nav-tabs mb-4" id="adminTabs" role="tablist">
            <li class="nav-item" role="presentation">
              <button class="nav-link active" id="users-tab" data-bs-toggle="tab" data-bs-target="#users" type="button" role="tab">
                Пользователи
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link" id="logs-tab" data-bs-toggle="tab" data-bs-target="#logs" type="button" role="tab">
                Журналы работ
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link" id="settings-tab" data-bs-toggle="tab" data-bs-target="#settings" type="button" role="tab">
                Настройки
              </button>
            </li>
          </ul>

          <!-- Контент табов -->
          <div class="tab-content">
            <!-- Таб пользователей -->
            <div class="tab-pane fade show active" id="users" role="tabpanel">
              <div class="card shadow-sm border-0">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                  <h5 class="mb-0">Управление пользователями</h5>
                  <a href="/addUser" class="btn btn-primary btn-sm">
                    <i class="bi bi-plus-lg me-1"></i>Добавить
                  </a>
                </div>
                <div class="card-body p-0">
                  <div class="table-responsive" style="max-height: 50vh;">
                    <table class="table table-hover mb-0">
                      <thead>
                      <tr>
                        <th>ID</th>
                        <th>Email</th>
                        <th>Роль</th>
                        <th>Дата регистрации</th>
                        <th>Действия</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="user in users" :key="user.id">
                        <td>{{ user.id }}</td>
                        <td>{{ user.email }}</td>
                        <td>
                          <span class="badge" :class="{'bg-primary': user.role === 'ROLE_ADMIN', 'bg-secondary': user.role === 'ROLE_USER'}">
                            {{ user.role === 'ROLE_ADMIN' ? 'Админ' : 'Пользователь' }}
                          </span>
                        </td>
                        <td>{{ user.createdAt }}</td>
                        <td>
                          <button @click="editUser(user)" class="btn btn-sm btn-outline-primary me-2">
                            <i class="bi bi-pencil"></i>
                          </button>
                          <button @click="confirmDeleteUser(user)" class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i>
                          </button>
                        </td>
                      </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>

            <!-- Таб журналов -->
            <div class="tab-pane fade" id="logs" role="tabpanel">
              <div class="card shadow-sm border-0">
                <div class="card-header bg-white">
                  <h5 class="mb-0">Журналы работ</h5>
                </div>
                <div class="card-body p-0">
                  <div class="table-responsive" style="max-height: 50vh;">
                    <table class="table table-hover mb-0">
                      <thead>
                      <tr>
                        <th>ID</th>
                        <th>Раздел</th>
                        <th>Дата создания</th>
                        <th>Статус</th>
                        <th>Действия</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="log in adminLogs" :key="log.id">
                        <td>{{ log.id }}</td>
                        <td>Раздел {{ log.section }}</td>
                        <td>{{ formatDate(log.createdAt) }}</td>
                        <td>
                          <span class="badge" :class="{'bg-success': log.status === 'COMPLETED', 'bg-warning': log.status === 'PENDING'}">
                            {{ log.status === 'COMPLETED' ? 'Завершен' : 'В процессе' }}
                          </span>
                        </td>
                        <td>
                          <button @click="downloadLog(log.id)" class="btn btn-sm btn-outline-primary me-2">
                            <i class="bi bi-download me-1"></i>Скачать
                          </button>
                          <button @click="deleteLog(log.id)" class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i>
                          </button>
                        </td>
                      </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>

            <!-- Таб настроек -->
            <div class="tab-pane fade" id="settings" role="tabpanel">
              <div class="card shadow-sm border-0">
                <div class="card-header bg-white">
                  <h5 class="mb-0">Настройки системы</h5>
                </div>
                <div class="card-body">
                  <form @submit.prevent="saveSettings">
                    <div class="mb-3">
                      <label class="form-label">Лимит журналов на пользователя</label>
                      <input type="number" class="form-control" v-model="settings.userLogLimit">
                    </div>
                    <div class="mb-3">
                      <label class="form-label">Автоматическое удаление старых журналов (дней)</label>
                      <input type="number" class="form-control" v-model="settings.autoDeleteDays">
                    </div>
                    <div class="mb-3 form-check">
                      <input type="checkbox" class="form-check-input" id="enableNotifications" v-model="settings.enableNotifications">
                      <label class="form-check-label" for="enableNotifications">Уведомления по email</label>
                    </div>
                    <button type="submit" class="btn btn-primary" :disabled="isSaving">
                      <span v-if="isSaving" class="spinner-border spinner-border-sm me-1"></span>
                      Сохранить
                    </button>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../components/Navbar.vue'
import Swal from 'sweetalert2';

const stats = ref({
  users: 0,
  journals: 0,
  pdfs: 0,
  errors: 0
})

const users = ref([])
const adminLogs = ref([])
const settings = ref({
  userLogLimit: 10,
  autoDeleteDays: 30,
  enableNotifications: true
})

const isSaving = ref(false)
const error = ref(null)

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('Требуется авторизация')
  }
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
}

const fetchUsers = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/users', {
      headers: getAuthHeaders()
    })
    if (response.ok) {
      users.value = await response.json()
    }
  } catch (err) {
    console.error("Ошибка загрузки пользователей:", err)
  }
}

const confirmDeleteUser = (user) => {
  const isCurrentUser = user.email === localStorage.getItem('userEmail');

  Swal.fire({
    title: 'Удаление пользователя',
    text: isCurrentUser
        ? 'Вы не можете удалить свой собственный аккаунт!'
        : `Вы уверены, что хотите удалить пользователя ${user.email}?`,
    icon: isCurrentUser ? 'warning' : 'question',
    showCancelButton: !isCurrentUser,
    confirmButtonText: isCurrentUser ? 'OK' : 'Да, удалить',
    cancelButtonText: 'Отмена',
    confirmButtonColor: '#d33',
  }).then((result) => {
    if (result.isConfirmed && !isCurrentUser) {
      deleteUser(user.id);
    }
  });
};

const deleteUser = async (userId) => {
  console.log(userId)
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      handleUnauthorized();
      return;
    }

    const response = await fetch(`http://localhost:8080/api/auth/users/${userId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      error.value = errorData.message || 'Ошибка при удалении пользователя';
      return;
    }

    // Показываем уведомление об успехе
    Swal.fire({
      title: 'Успешно!',
      text: 'Пользователь был удален',
      icon: 'success',
      timer: 2000,
      showConfirmButton: false
    });

    // Обновляем список пользователей
    await fetchUsers();
  } catch (err) {
    console.error("Ошибка удаления пользователя:", err);
    Swal.fire({
      title: 'Ошибка!',
      text: err.message || 'Не удалось удалить пользователя',
      icon: 'error'
    });

    if (err.message.includes('401') || err.message.includes('авторизация')) {
      handleUnauthorized();
    }
  }
};

const downloadLog = async (logId) => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      handleUnauthorized()
      return
    }

    const pdfWindow = window.open('', '_blank')
    const response = await fetch(`http://localhost:8080/admin/logs/${logId}/download`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.status === 401) {
      handleUnauthorized()
      pdfWindow.close()
      return
    }

    const blob = await response.blob()
    pdfWindow.location.href = URL.createObjectURL(blob)
  } catch (err) {
    console.error('Ошибка при загрузке журнала:', err)
    error.value = 'Не удалось загрузить журнал'
  }
}

const deleteLog = async (logId) => {
  if (confirm('Удалить этот журнал?')) {
    try {
      const response = await fetch(`http://localhost:8080/admin/logs/${logId}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      })
      if (response.ok) {
        await fetchLogs()
      }
    } catch (err) {
      console.error("Ошибка удаления журнала:", err)
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ru-RU')
}

onMounted(() => {
  // fetchStats()
  fetchUsers()
  // fetchLogs()
  // fetchSettings()
})
</script>

<style scoped>
/* Основные стили */
body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Карточки статистики */
.card {
  transition: transform 0.2s;
  border-radius: 8px;
}

.card:hover {
  transform: translateY(-3px);
}

/* Табы */
.nav-tabs .nav-link {
  color: #495057;
  font-weight: 500;
}

.nav-tabs .nav-link.active {
  color: #0d6efd;
  border-bottom: 3px solid #0d6efd;
}

/* Таблицы */
.table {
  font-size: 0.9rem;
}

.table th {
  font-weight: 500;
  letter-spacing: 0.5px;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

/* Скролл таблицы */
.table-responsive {
  scrollbar-width: thin;
  scrollbar-color: #002d72 #f1f1f1;
}

.table-responsive::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.table-responsive::-webkit-scrollbar-thumb {
  background-color: #002d72;
  border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

/* Бейджи */
.badge {
  font-weight: 500;
  padding: 5px 10px;
}

/* Модальное окно */
.modal-content {
  border-radius: 10px;
}

/* Анимации */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.table tbody tr {
  animation: fadeIn 0.3s ease forwards;
}
</style>