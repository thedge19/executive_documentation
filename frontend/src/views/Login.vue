<template>
  <main class="min-vh-100 d-flex align-items-center" :style="{'background-image':'url(/09-12-2016_yuzhno-russkoe_2.jpg)'}">
    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 500px; background-color: rgba(255, 255, 255, 0.9);">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Вход в систему</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="handleLogin">
            <!-- Поле для логина -->
            <div class="mb-4">
              <label for="email" class="form-label fw-semibold">
                <i class="bi bi-envelope me-2"></i>Email
              </label>
              <input
                  id="email"
                  type="email"
                  class="form-control"
                  placeholder="Введите ваш email"
                  required
                  v-model="email"
              >
            </div>

            <!-- Поле для пароля -->
            <div class="mb-4">
              <label for="password" class="form-label fw-semibold">
                <i class="bi bi-lock me-2"></i>Пароль
              </label>
              <input
                  id="password"
                  type="password"
                  class="form-control"
                  placeholder="Введите ваш пароль"
                  required
                  v-model="password"
              >
            </div>

            <!-- Сообщение об ошибке -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопка входа -->
            <div class="d-grid">
              <button type="submit" class="btn btn-primary py-2" :disabled="loading">
                <template v-if="loading">
                  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Вход...
                </template>
                <template v-else>
                  <i class="bi bi-box-arrow-in-right me-2"></i>Войти
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  try {
    loading.value = true
    error.value = ''

    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: email.value,
        password: password.value
      })
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка входа. Проверьте данные и попробуйте снова.'
      return
    }

    const data = await response.json()
    const token = data.token || data.accessToken

    if (!token) {
      error.value = 'Не удалось получить токен авторизации'
      return
    }

    localStorage.setItem('token', token)
    await router.push('/')
  } catch (err) {
    error.value = err.message || 'Произошла ошибка при входе в систему'
    console.error('Login error:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card {
  border-radius: 0.5rem;
  backdrop-filter: blur(5px);
}

.card-header {
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.form-control {
  padding: 0.75rem 1rem;
  border-radius: 0.375rem;
  border: 1px solid #ced4da;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.form-control:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.1);
}

.btn-primary {
  background-color: #0d6efd;
  border-color: #0d6efd;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background-color: #0b5ed7;
  border-color: #0a58ca;
}

.alert {
  border-radius: 0.375rem;
  padding: 0.75rem 1rem;
}

.text-primary {
  color: #0d6efd !important;
}

/* Анимация для плавного появления */
main {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>