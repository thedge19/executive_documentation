<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Добавить объект</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="addProject">
            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-building me-2"></i>Наименование объекта
              </label>
              <input id="name" type="text" class="form-control"
                     placeholder="Введите наименование объекта"
                     required v-model="project.name">
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопка отправки -->
            <div class="d-grid">
              <button type="submit" class="btn btn-primary py-2">
                <i class="bi bi-check-circle me-2"></i>Добавить объект
              </button>
            </div>
            <div v-if="error" class="error-message">
              {{ error }}
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'AddProject',
  components: {
    Navbar
  },
  data() {
    return {
      project: {
        name: '',
      },
      error: null
    }
  },
  methods: {
    async addProject() {
      this.error = null
      this.isLoading = true

      try {
        const token = localStorage.getItem('token')
        if (!token) {
          throw new Error('Требуется авторизация')
        }

        const response = await fetch('http://localhost:8080/projects', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(this.project)
        })

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}))
          throw new Error(errorData.message || 'Ошибка при добавлении объекта')
        }

        // Успешное создание
        this.$router.push('/projects')
      } catch (error) {
        console.error('Ошибка:', error)
        this.error = error.message

        // Если 401 - перенаправляем на логин
        if (error.message.includes('401') || error.message.includes('авторизация')) {
          this.$router.push('/login?redirect=' + encodeURIComponent(this.$route.fullPath))
        }
      } finally {
        this.isLoading = false
      }
    }
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
}
</style>