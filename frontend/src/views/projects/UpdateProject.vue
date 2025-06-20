<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Обновить объект</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="updateProject">
            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-building me-2"></i>Наименование объекта
              </label>
              <input id="name"
                     type="text"
                     class="form-control"
                     placeholder="Введите наименование объекта"
                     required
                     v-model="project.name">
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопка отправки -->
            <div class="d-grid">
              <button type="submit"
                      class="btn btn-primary py-2"
                      :disabled="isLoading">
                <template v-if="!isLoading">
                  <i class="bi bi-check-circle me-2"></i>Обновить объект
                </template>
                <template v-else>
                  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Сохранение...
                </template>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'UpdateProject',
  components: {
    Navbar
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const project = ref({
      id: '',
      name: ''
    });

    const isLoading = ref(false);
    const error = ref(null);

    const getProject = async () => {
      try {
        isLoading.value = true;
        error.value = null;

        const token = localStorage.getItem('token');
        if (!token) {
          error.value = 'Требуется авторизация';
          return
        }

        const response = await fetch(`http://localhost:8080/projects/${route.params.id}`, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.status === 401) {
          await router.push('/login?redirect=' + encodeURIComponent(route.fullPath));
          return;
        }

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          error.value = errorData.message || `Ошибка ${response.status}`;
          return
        }

        project.value = await response.json();
      } catch (err) {
        error.value = err.message;
        console.error('Ошибка при загрузке проекта:', err);
      } finally {
        isLoading.value = false;
      }
    };

    const updateProject = async () => {
      try {
        isLoading.value = true;
        error.value = null;

        const token = localStorage.getItem('token');
        if (!token) {
          error.value = 'Требуется авторизация';
          return
        }

        const response = await fetch(`http://localhost:8080/projects/${route.params.id}`, {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(project.value)
        });

        if (response.status === 401) {
          await router.push('/login?redirect=' + encodeURIComponent(route.fullPath));
          return;
        }

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          error.value = errorData.message || `Ошибка ${response.status}`;
          return;
        }

        await response.json();
        await router.push('/projects');
      } catch (err) {
        error.value = err.message;
        console.error('Ошибка при обновлении проекта:', err);
      } finally {
        isLoading.value = false;
      }
    };

    onMounted(() => {
      getProject();
    });

    return {
      project,
      isLoading,
      error,
      updateProject
    };
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
  border: 1px solid #dee2e6;
}

.form-control:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}

.form-label {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  color: #495057;
}

.btn {
  border-radius: 8px;
  transition: all 0.2s;
  font-weight: 500;
}

.btn-primary {
  background-color: #0d6efd;
  border-color: #0d6efd;
}

.btn-primary:hover {
  background-color: #0b5ed7;
  border-color: #0a58ca;
}

.btn-primary:disabled {
  background-color: #86b7fe;
  border-color: #86b7fe;
}

.alert {
  border-radius: 8px;
  padding: 12px 16px;
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