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
                  <h5 class="card-title"><i class="bi bi-journal-text me-2"></i>Выполнение</h5>
                  <p class="card-text display-5">{{ globalStats }} %</p>
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
                Выполнение
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link" id="errors-tab" data-bs-toggle="tab" data-bs-target="#errors" type="button" role="tab">
                Ошибки
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button class="nav-link" id="settings-tab" data-bs-toggle="tab" data-bs-target="#settings" type="button" role="tab">
                Деньги
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

            <!-- Таб выполнения -->
            <div class="tab-pane fade" id="logs" role="tabpanel">
              <div class="card shadow-sm border-0">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                  <h5 class="mb-0">Статистика выполнения работ по подобъектам</h5>
                  <button @click="fetchActStats" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-arrow-clockwise"></i> Обновить
                  </button>
                </div>
                <div class="card-body p-0">
                  <div class="table-responsive" style="max-height: 30vh;">
                    <table class="table table-hover mb-0">
                      <thead class="sticky-top" style="background-color: #002d72;">
                      <tr>
                        <th class="text-white fw-normal" style="width: 40%; background-color: #000000;">Подобъект</th>
                        <th class="text-white fw-normal" style="width: 40%; background-color: #000000;">Выполнение</th>
                        <th class="text-center text-white fw-normal" style="width: 10%; background-color: #000000;">Процент</th>
                        <th class="text-center text-white fw-normal" style="width: 10%; background-color: #000000;">Акты/Работы</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="(stats, key) in actStats" :key="key" class="table-light">
                        <td class="align-middle">{{ key }}</td>
                        <td class="align-middle">
                          <div class="progress" style="height: 20px;">
                            <div
                                class="progress-bar"
                                role="progressbar"
                                :style="{ width: stats.percentage + '%' }"
                                :class="{
                      'bg-success': stats.percentage >= 75,
                      'bg-warning': stats.percentage >= 25 && stats.percentage < 75,
                      'bg-danger': stats.percentage < 25
                    }"
                                :aria-valuenow="stats.percentage"
                                aria-valuemin="0"
                                aria-valuemax="100"
                            ></div>
                          </div>
                        </td>
                        <td class="text-center align-middle">{{ stats.percentage.toFixed(1) }}%</td>
                        <td class="text-center align-middle">{{ stats.actCount }}/{{ stats.workCount }}</td>
                      </tr>
                      </tbody>
                    </table>
                  </div>

                  <!-- График выполнения -->
                  <div class="mt-4 p-3">
                    <div class="card">
                      <div class="card-header bg-white">
                        <h6 class="mb-0">График выполнения работ</h6>
                      </div>
                      <div class="card-body">
                        <div class="chart-container" style="height: 250px;">
                          <canvas ref="actStatsChart"></canvas>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
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
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                  <h5 class="mb-0">Финансовая статистика по подобъектам</h5>
                  <button @click="fetchFinancialStats" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-arrow-clockwise"></i> Обновить
                  </button>
                </div>
                <div class="card-body p-0">
                  <div class="table-responsive" style="max-height: 30vh;">
                    <table class="table table-hover mb-0">
                      <thead class="sticky-top" style="background-color: #002d72;">
                      <tr>
                        <th class="text-white fw-normal" style="width: 40%; background-color: #000000;">Подобъект</th>
                        <th class="text-white fw-normal" style="width: 40%; background-color: #000000;">Выполнение</th>
                        <th class="text-center text-white fw-normal" style="width: 10%; background-color: #000000;">Процент</th>
                        <th class="text-center text-white fw-normal" style="width: 10%; background-color: #000000;">Сумма/Всего</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="(stats, key) in financialStats" :key="key" class="table-light">
                        <td class="align-middle">{{ key }}</td>
                        <td class="align-middle">
                          <div class="progress" style="height: 20px;">
                            <div
                                class="progress-bar"
                                role="progressbar"
                                :style="{ width: stats.percentage + '%' }"
                                :class="{
                      'bg-success': stats.percentage >= 75,
                      'bg-warning': stats.percentage >= 25 && stats.percentage < 75,
                      'bg-danger': stats.percentage < 25
                    }"
                                :aria-valuenow="stats.percentage"
                                aria-valuemin="0"
                                aria-valuemax="100"
                            ></div>
                          </div>
                        </td>
                        <td class="text-center align-middle">{{ stats.percentage.toFixed(1) }}%</td>
                        <td class="text-center align-middle">{{ stats.doneAmount.toFixed(2) }}/{{ stats.totalAmount.toFixed(2) }}</td>
                      </tr>
                      </tbody>
                    </table>
                  </div>

                  <!-- График финансовой статистики -->
                  <div class="mt-4 p-3">
                    <div class="card">
                      <div class="card-header bg-white">
                        <h6 class="mb-0">Финансовое выполнение работ</h6>
                      </div>
                      <div class="card-body">
                        <div class="chart-container" style="height: 250px;">
                          <canvas ref="financialStatsChart"></canvas>
                        </div>
                      </div>
                    </div>
                  </div>
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
import {nextTick, onMounted, ref} from 'vue'
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
const error = ref("")
let chartInstance = null

const actStats = ref({});
const actStatsChart = ref(null);
let actChartInstance = null;

const financialStats = ref({});
const financialStatsChart = ref(null);
let financialChartInstance = null;

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
      error.value = `Ошибка HTTP: ${response.status}`;
      return;
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
        return;
      }
      error.value = `Ошибка HTTP: ${response.status}`;
      return;
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

const globalStats = ref(0); // Добавляем новую ref для хранения глобальной статистики

// Добавляем новую функцию для получения глобальной статистики
const fetchGlobalStats = async () => {
  if (!checkAuth()) return;

  try {
    const response = await fetch('http://localhost:8080/acts/globalStats', {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized();
        return;
      }
      error.value = `Ошибка HTTP: ${response.status}`;
      return;
    }

    globalStats.value = await response.json();
  } catch (err) {
    console.error('Ошибка загрузки глобальной статистики:', err);
    Swal.fire('Ошибка', 'Не удалось загрузить общую статистику выполнения', 'error');
  }
};

// Загрузка статистики актов
const fetchActStats = async () => {
  if (!checkAuth()) return;

  try {
    const response = await fetch('http://localhost:8080/acts/stats', {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized();
        return;
      }
      error.value = `Ошибка HTTP: ${response.status}`;
      return;
    }

    const statsData = await response.json();

    // Получаем отдельно количество работ по подобъектам
    const worksResponse = await fetch('http://localhost:8080/workings/count-by-subobject', {
      headers: getAuthHeaders()
    });
    const worksCounts = worksResponse.ok ? await worksResponse.json() : {};

    // Формируем данные для отображения
    actStats.value = Object.keys(worksCounts).reduce((acc, key) => {
      const workCount = worksCounts[key] || 0;
      const percentage = statsData[key] || 0;
      acc[key] = {
        percentage: percentage,
        actCount: Math.round(percentage * workCount / 100),
        workCount: workCount
      };
      return acc;
    }, {});

    // Добавляем подобъекты, которые есть в актах, но нет в работах
    Object.keys(statsData).forEach(key => {
      if (!actStats.value[key]) {
        actStats.value[key] = {
          percentage: statsData[key],
          actCount: 0,
          workCount: 0
        };
      }
    });

    updateActStatsChart();
  } catch (err) {
    console.error('Ошибка загрузки статистики актов:', err);
    Swal.fire('Ошибка', 'Не удалось загрузить статистику выполнения работ', 'error');
  }
};

const updateActStatsChart = () => {
  nextTick(() => {
    if (actChartInstance) {
      actChartInstance.destroy();
    }

    const ctx = actStatsChart.value.getContext('2d');
    const labels = Object.keys(actStats.value);
    const data = labels.map(key => actStats.value[key].percentage);

    // Сортируем данные для лучшего отображения
    const sortedIndices = [...Array(labels.length).keys()]
        .sort((a, b) => data[b] - data[a]);

    const sortedLabels = sortedIndices.map(i => labels[i]);
    const sortedData = sortedIndices.map(i => data[i]);
    const backgroundColors = sortedData.map(value =>
        value >= 75 ? '#28a745' :
            value >= 25 ? '#ffc107' :
                '#dc3545'
    );

    actChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: sortedLabels,
        datasets: [{
          label: 'Процент выполнения',
          data: sortedData,
          backgroundColor: backgroundColors,
          borderColor: backgroundColors.map(c => shadeColor(c, -20)),
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            max: 100,
            title: {
              display: true,
              text: 'Процент выполнения (%)'
            }
          },
          x: {
            title: {
              display: true,
              text: 'Подобъекты'
            }
          }
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: function(context) {
                const stats = actStats.value[sortedLabels[context.dataIndex]];
                return [
                  `Процент: ${context.raw.toFixed(1)}%`,
                  `Актов: ${stats.actCount}`,
                  `Работ: ${stats.workCount}`
                ];
              }
            }
          }
        }
      }
    });
  });
};

// Вспомогательная функция для затемнения цвета
const shadeColor = (color, percent) => {
  let R = parseInt(color.substring(1,3), 16);
  let G = parseInt(color.substring(3,5), 16);
  let B = parseInt(color.substring(5,7), 16);

  R = parseInt(R * (100 + percent) / 100);
  G = parseInt(G * (100 + percent) / 100);
  B = parseInt(B * (100 + percent) / 100);

  R = (R<255)?R:255;
  G = (G<255)?G:255;
  B = (B<255)?B:255;

  R = Math.round(R);
  G = Math.round(G);
  B = Math.round(B);

  const RR = ((R.toString(16).length===1)?"0"+R.toString(16):R.toString(16));
  const GG = ((G.toString(16).length===1)?"0"+G.toString(16):G.toString(16));
  const BB = ((B.toString(16).length===1)?"0"+B.toString(16):B.toString(16));

  return "#"+RR+GG+BB;
};

const fetchFinancialStats = async () => {
  if (!checkAuth()) return;

  try {
    const response = await fetch('http://localhost:8080/workings/financial-stats', {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized();
        return;
      }
      error.value = `Ошибка HTTP: ${response.status}`;
      return;
    }

    financialStats.value = await response.json();

    // Рассчитываем проценты выполнения
    Object.keys(financialStats.value).forEach(key => {
      const stats = financialStats.value[key];
      stats.percentage = (stats.doneAmount / stats.totalAmount) * 100;
    });

    updateFinancialStatsChart();
  } catch (err) {
    console.error('Ошибка загрузки финансовой статистики:', err);
    Swal.fire('Ошибка', 'Не удалось загрузить финансовую статистику', 'error');
  }
};

const updateFinancialStatsChart = () => {
  nextTick(() => {
    if (financialChartInstance) {
      financialChartInstance.destroy();
    }

    const ctx = financialStatsChart.value.getContext('2d');
    const labels = Object.keys(financialStats.value);
    const data = labels.map(key => financialStats.value[key].percentage);

    // Сортируем данные для лучшего отображения
    const sortedIndices = [...Array(labels.length).keys()]
        .sort((a, b) => data[b] - data[a]);

    const sortedLabels = sortedIndices.map(i => labels[i]);
    const sortedData = sortedIndices.map(i => data[i]);
    const backgroundColors = sortedData.map(value =>
        value >= 75 ? '#28a745' :
            value >= 25 ? '#ffc107' :
                '#dc3545'
    );

    financialChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: sortedLabels,
        datasets: [{
          label: 'Процент выполнения (финансы)',
          data: sortedData,
          backgroundColor: backgroundColors,
          borderColor: backgroundColors.map(c => shadeColor(c, -20)),
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            max: 100,
            title: {
              display: true,
              text: 'Процент выполнения (%)'
            }
          },
          x: {
            title: {
              display: true,
              text: 'Подобъекты'
            }
          }
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: function(context) {
                const stats = financialStats.value[sortedLabels[context.dataIndex]];
                return [
                  `Процент: ${context.raw.toFixed(1)}%`,
                  `Выполнено: ${stats.doneAmount.toFixed(2)} руб.`,
                  `Всего: ${stats.totalAmount.toFixed(2)} руб.`
                ];
              }
            }
          }
        }
      }
    });
  });
};

onMounted(async () => {
  if (!checkAuth()) return;

  isLoading.value = true;
  try {
    await Promise.all([
      fetchUsers(),
      fetchErrorStats(),
      fetchActStats(),
      fetchGlobalStats(),
      fetchFinancialStats() // Добавляем загрузку финансовой статистики
    ]);
  } catch (err) {
    console.error('Ошибка инициализации:', err);
  } finally {
    isLoading.value = false;
  }
});
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