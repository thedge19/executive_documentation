<template>
  <main class="bg-light min-vh-100 d-flex align-items-center">
    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 500px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Вход в систему</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="handleLogin">
            <!-- Поле для email/username -->
            <div class="mb-4">
              <label for="username" class="form-label fw-semibold">
                <i class="bi bi-person me-2"></i>Логин
              </label>
              <input
                  id="username"
                  type="text"
                  class="form-control"
                  placeholder="Введите ваш логин"
                  required
                  v-model="username"
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
              <button type="submit" class="btn btn-primary py-2">
                <i class="bi bi-box-arrow-in-right me-2"></i>Войти
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: '',
      error: ''
    };
  },
  methods: {
    async handleLogin() {
      try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: this.username,
            password: this.password
          })
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          this.error = errorData.message || 'Ошибка входа. Проверьте данные и попробуйте снова.';
          return;
        }

        const data = await response.json();
        const token = data.token || data.accessToken;

        if (!token) {
          throw new Error('Не удалось получить токен авторизации');
        }

        localStorage.setItem('token', token);
        this.$router.push('/');
      } catch (err) {
        this.error = err.message || 'Произошла ошибка при входе в систему';
        console.error('Login error:', err);
      }
    }
  }
};
</script>

<style scoped>
.card {
  border-radius: 0.5rem;
}

.card-header {
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.form-control {
  padding: 0.75rem 1rem;
  border-radius: 0.375rem;
  border: 1px solid #ced4da;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.form-control:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
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

.bg-light {
  background-color: #f8f9fa !important;
}
</style>