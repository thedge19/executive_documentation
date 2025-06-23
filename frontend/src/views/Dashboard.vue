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
                  <p class="card-text display-5">{{ errorStats.totalErrors }}</p>
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
              <button class="nav-link" id="errors-tab" data-bs-toggle="tab" data-bs-target="#errors" type="button" role="tab">
                Ошибки
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
                        <th>Имя</th>
                        <th>Email</th>
                        <th>Роль</th>
                        <th>Дата регистрации</th>
                        <th>Действия</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="user in users" :key="user.id">
                        <td>{{ user.id }}</td>
                        <td>{{ user.username }}</td>
                        <td>{{ user.email }}</td>
                        <td>
                          <span class="badge" :class="{'bg-primary': user.role === 'ROLE_ADMIN', 'bg-secondary': user.role === 'ROLE_USER'}">
                            {{ user.role === 'ROLE_ADMIN' ? 'Админ' : 'Пользователь' }}
                          </span>
                        </td>
                        <td>{{ user.createdAt }}</td>
                        <td>
                          <a :href="`/editUser/${user.id}`" class="btn btn-sm btn-outline-primary me-2">
                            <i class="bi bi-pencil"></i>
                          </a>
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
              <!-- ... существующий код ... -->
            </div>

            <!-- Новый таб ошибок -->
            <div class="tab-pane fade" id="errors" role="tabpanel">
              <div class="card shadow-sm border-0">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                  <h5 class="mb-0">Статистика ошибок</h5>
                  <button @click="fetchErrorStats" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-arrow-clockwise"></i> Обновить
                  </button>
                </div>
                <div class="card-body">
                  <div class="row mb-4">
                    <div class="col-md-4">
                      <div class="card bg-light">
                        <div class="card-body">
                          <h6 class="card-title text-center">За последние 24 часа</h6>
                          <p class="card-text display-4 text-center text-danger">{{ errorStats.last24Hours }}</p>
                        </div>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="card bg-light">
                        <div class="card-body">
                          <h6 class="card-title text-center">За последние 7 дней</h6>
                          <p class="card-text display-4 text-center text-warning">{{ errorStats.last7Days }}</p>
                        </div>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="card bg-light">
                        <div class="card-body">
                          <h6 class="card-title text-center">Всего ошибок</h6>
                          <p class="card-text display-4 text-center text-primary">{{ errorStats.totalErrors }}</p>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="row">
                    <div class="col-md-6">
                      <div class="card">
                        <div class="card-header bg-white">
                          <h6 class="mb-0">Распределение по уровням</h6>
                        </div>
                        <div class="card-body">
                          <div class="chart-container" style="height: 250px;">
                            <canvas ref="levelChart"></canvas>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="card">
                        <div class="card-header bg-white">
                          <h6 class="mb-0">Частые ошибки</h6>
                        </div>
                        <div class="card-body">
                          <ul class="list-group">
                            <li class="list-group-item d-flex justify-content-between align-items-center">
                              Самая частая ошибка
                              <span class="badge bg-primary rounded-pill">{{ errorStats.mostCommonErrorMessage }}</span>
                            </li>
                            <li class="list-group-item d-flex justify-content-between align-items-center">
                              Самый проблемный endpoint
                              <span class="badge bg-primary rounded-pill">{{ errorStats.mostFrequentEndpoint }}</span>
                            </li>
                          </ul>
                        </div>
                      </div>
                    </div>
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
                    <button type="submit" class="btn btn-primary">
                      <span class="spinner-border spinner-border-sm me-1"></span>
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
import { ref, onMounted, nextTick } from 'vue'
import Navbar from '../components/Navbar.vue'
import Swal from 'sweetalert2'
import Chart from 'chart.js/auto'

const stats = ref({
  users: 0,
  journals: 0,
  pdfs: 0,
  errors: 0
})

const users = ref([])
const errorStats = ref({
  totalErrors: 0,
  last24Hours: 0,
  last7Days: 0,
  countByLevel: {},
  countByDay: {},
  mostCommonErrorMessage: '',
  mostFrequentEndpoint: ''
})
const settings = ref({
  userLogLimit: 10,
  autoDeleteDays: 30,
  enableNotifications: true
})
const isLoading = ref(true)
const levelChart = ref(null)
let chartInstance = null

// Улучшенная проверка аутентификации
const checkAuth = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    handleUnauthorized()
    return false
  }
  return true
}

// Получение заголовков с авторизацией
const getAuthHeaders = () => {
  return {
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  }
}

// Обработка неавторизованного доступа
const handleUnauthorized = () => {
  localStorage.removeItem('token')
  window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
}


// Загрузка пользователей
const fetchUsers = async () => {
  if (!checkAuth()) return

  try {
    const response = await fetch('http://localhost:8080/api/auth/users', {
      headers: getAuthHeaders()
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      throw new Error(`Ошибка HTTP: ${response.status}`)
    }

    users.value = await response.json()
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

// Загрузка статистики ошибок
const fetchErrorStats = async () => {
  if (!checkAuth()) return

  try {
    const response = await fetch('http://localhost:8080/errors/stats', {
      headers: getAuthHeaders()
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      throw new Error(`Ошибка HTTP: ${response.status}`)
    }

    errorStats.value = await response.json()
    updateChart()
  } catch (err) {
    console.error('Ошибка загрузки статистики:', err)
    Swal.fire('Ошибка', 'Не удалось загрузить статистику ошибок', 'error')
  }
}

const updateChart = () => {
  nextTick(() => {
    if (chartInstance) {
      chartInstance.destroy()
    }

    const ctx = levelChart.value.getContext('2d')
    chartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: Object.keys(errorStats.value.countByLevel),
        datasets: [{
          data: Object.values(errorStats.value.countByLevel),
          backgroundColor: [
            '#4e73df', // INFO
            '#f6c23e', // WARNING
            '#e74a3b', // ERROR
            '#5a5c69'  // CRITICAL
          ],
          hoverBackgroundColor: [
            '#2e59d9',
            '#dda20a',
            '#be2617',
            '#373840'
          ],
          hoverBorderColor: "rgba(234, 236, 244, 1)",
        }]
      },
      options: {
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right'
          }
        },
        cutout: '70%'
      }
    })
  })
}

onMounted(async () => {
  if (!checkAuth()) return

  isLoading.value = true
  try {
    await Promise.all([fetchUsers(), fetchErrorStats()])
  } catch (err) {
    console.error('Ошибка инициализации:', err)
  } finally {
    isLoading.value = false
  }
})
</script>

<style scoped>
.chart-container {
  position: relative;
  min-height: 250px;
}

.card {
  transition: transform 0.2s;
  border-radius: 8px;
}

.card:hover {
  transform: translateY(-3px);
}

.nav-tabs .nav-link {
  color: white;
  font-weight: 500;
}

.nav-tabs .nav-link.active {
  color: #0d6efd;
  border-bottom: 3px solid #0d6efd;
}

.table th {
  font-weight: 500;
  letter-spacing: 1px;
}

.badge {
  font-weight: 500;
  padding: 5px 10px;
}

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