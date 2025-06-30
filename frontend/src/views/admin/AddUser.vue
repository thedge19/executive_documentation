<template>
  <Navbar/>

  <div class="container py-5">
    <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
      <div class="card-header bg-white py-4">
        <h2 class="h4 mb-0 text-center text-primary">Добавить пользователя</h2>
      </div>

      <div class="card-body">
        <form autocomplete="off" @submit.prevent="addUser">
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

          <!-- Пароль -->
          <div class="mb-4">
            <label for="password" class="form-label fw-semibold">
              <i class="bi bi-lock me-2"></i>Пароль
            </label>
            <input id="password" type="password" class="form-control"
                   placeholder="Введите пароль"
                   required v-model="user.password">
          </div>

          <!-- Подтверждение пароля -->
          <div class="mb-4">
            <label for="confirmPassword" class="form-label fw-semibold">
              <i class="bi bi-lock-fill me-2"></i>Подтверждение пароля
            </label>
            <input id="confirmPassword" type="password" class="form-control"
                   placeholder="Повторите пароль"
                   required v-model="user.confirmPassword"
                   @blur="validatePassword">
            <div v-if="passwordMismatch" class="text-danger small mt-2">
              <i class="bi bi-exclamation-circle me-1"></i>Пароли не совпадают
            </div>
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
            <button type="button" @click="resetForm"
                    class="btn btn-outline-secondary flex-grow-1 py-2"
                    :disabled="isLoading">
              <i class="bi bi-arrow-counterclockwise me-2"></i>Сбросить
            </button>

            <button type="submit" class="btn btn-primary flex-grow-1 py-2"
                    :disabled="isLoading || passwordMismatch">
              <template v-if="isLoading">
                <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Добавление...
              </template>
              <template v-else>
                <i class="bi bi-person-plus me-2"></i>Добавить
              </template>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const user = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'ROLE_USER'
})
const error = ref(null)
const isLoading = ref(false)
const passwordMismatch = ref(false)

const validatePassword = () => {
  passwordMismatch.value = user.value.password !== user.value.confirmPassword
}

const resetForm = () => {
  user.value = {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'ROLE_USER'
  }
  error.value = null
  passwordMismatch.value = false
}

const addUser = async () => {
  error.value = null
  isLoading.value = true

  // Проверка совпадения паролей
  if (user.value.password !== user.value.confirmPassword) {
    error.value = 'Пароли не совпадают'
    isLoading.value = false
    return
  }

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      error.value = 'Требуется авторизация'
      await router.push('/login')
      return
    }

    const response = await fetch('http://localhost:8080/api/auth/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        username: user.value.username,
        email: user.value.email,
        password: user.value.password,
        role: user.value.role
      })
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при добавлении пользователя'

      if (response.status === 401) {
        localStorage.removeItem('token')
        await router.push('/login')
      }
      return
    }

    // Успешное добавление - перенаправляем на список пользователей
    await router.push('/dashboard')
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message || 'Произошла ошибка при отправке данных'

    if (err.message.includes('401') || err.message.includes('авторизация')) {
      await router.push('/login')
    }
  } finally {
    isLoading.value = false
  }
}
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