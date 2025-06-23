<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Редактирование пользователя</h2>
        </div>

        <div class="card-body">
          <form autocomplete="off" @submit.prevent="updateUser">
            <!-- Имя пользователя -->
            <div class="mb-4">
              <label for="username" class="form-label fw-semibold">
                <i class="bi bi-person-badge me-2"></i>Имя пользователя
              </label>
              <input id="username" type="text" class="form-control"
                     placeholder="Введите имя пользователя"
                     required v-model="user.username">
            </div>

            <!-- Email -->
            <div class="mb-4">
              <label for="email" class="form-label fw-semibold">
                <i class="bi bi-envelope me-2"></i>Email
              </label>
              <input id="email" type="email" class="form-control"
                     placeholder="Введите email"
                     required v-model="user.email">
            </div>

            <!-- Выбор роли -->
            <div class="mb-4">
              <label class="form-label fw-semibold d-block mb-3">
                <i class="bi bi-shield me-2"></i>Роль пользователя
              </label>
              <div class="btn-group w-100" role="group">
                <input type="radio" class="btn-check" name="role"
                       id="roleUser" autocomplete="off"
                       value="ROLE_USER" v-model="user.role">
                <label class="btn btn-outline-primary" for="roleUser">
                  <i class="bi bi-person me-2"></i>Обычный пользователь
                </label>

                <input type="radio" class="btn-check" name="role"
                       id="roleAdmin" autocomplete="off"
                       value="ROLE_ADMIN" v-model="user.role">
                <label class="btn btn-outline-primary" for="roleAdmin">
                  <i class="bi bi-shield-lock me-2"></i>Администратор
                </label>
              </div>
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопки -->
            <div class="d-flex gap-3">
              <router-link to="/dashboard" class="btn btn-outline-secondary flex-grow-1 py-2">
                <i class="bi bi-arrow-left me-2"></i>Назад
              </router-link>

              <button type="submit" class="btn btn-primary flex-grow-1 py-2"
                      :disabled="isLoading">
                <template v-if="isLoading">
                  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Сохранение...
                </template>
                <template v-else>
                  <i class="bi bi-save me-2"></i>Сохранить
                </template>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Navbar from '../../components/Navbar.vue'
import Swal from 'sweetalert2'

const router = useRouter()
const route = useRoute()
const user = ref({
  id: null,
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'ROLE_USER'
})
const error = ref(null)
const isLoading = ref(false)

// Получаем ID пользователя из URL
const userId = route.params.id

// Загрузка данных пользователя
const fetchUser = async () => {
  if (!checkAuth()) return

  isLoading.value = true
  try {
    const response = await fetch(`http://localhost:8080/api/auth/users/${userId}`, {
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

    const userData = await response.json()
    user.value = {
      id: userData.id,
      username: userData.username,
      email: userData.email,
      role: userData.role
    }
  } catch (err) {
    console.error("Ошибка загрузки пользователя:", err)
    error.value = "Не удалось загрузить данные пользователя"
    Swal.fire('Ошибка', 'Не удалось загрузить данные пользователя', 'error')
  } finally {
    isLoading.value = false
  }
}

// Проверка аутентификации
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
  router.push('/login?redirect=' + encodeURIComponent(route.path))
}

// Обновление пользователя
const updateUser = async () => {
  error.value = null
  isLoading.value = true

  try {
    const response = await fetch(`http://localhost:8080/api/auth/users/${userId}`, {
      method: 'PATCH',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        username: user.value.username,
        email: user.value.email,
        password: user.value.password || null, // Отправляем null, если пароль не меняется
        role: user.value.role
      })
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при обновлении пользователя';
      return;
    }

    // Успешное обновление - перенаправляем на список пользователей
    Swal.fire({
      title: 'Успешно!',
      text: 'Пользователь обновлен',
      icon: 'success',
      timer: 2000,
      showConfirmButton: false
    }).then(() => {
      router.push('/dashboard')
    })
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message || 'Произошла ошибка при обновлении данных'

    if (err.message.includes('401') || err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchUser()
})
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.form-control {
  border-radius: 8px;
  padding: 10px 15px;
}

.form-label {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.btn {
  border-radius: 8px;
  transition: all 0.2s;
}

.alert {
  border-radius: 8px;
}

.btn-group {
  gap: 8px;
}

.btn-group .btn {
  flex: 1;
}

@media (max-width: 576px) {
  .card {
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .container {
    padding-left: 0;
    padding-right: 0;
  }

  .d-flex {
    flex-direction: column;
    gap: 12px;
  }
}
</style>