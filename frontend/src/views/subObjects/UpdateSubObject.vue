<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Редактировать подобъект</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="updateSubObject">
            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-building me-2"></i>Наименование
              </label>
              <input id="name" type="text" class="form-control"
                     placeholder="Введите наименование подобъекта"
                     required v-model="subObject.name">
            </div>

            <!-- Аббревиатура -->
            <div class="mb-4">
              <label for="title" class="form-label fw-semibold">
                <i class="bi bi-textarea-t me-2"></i>Аббревиатура
              </label>
              <input id="title" type="text" class="form-control"
                     placeholder="Введите аббревиатуру"
                     required v-model="subObject.title">
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопки -->
            <div class="d-flex gap-3">
              <button type="button" @click="router.back()"
                      class="btn btn-outline-secondary flex-grow-1 py-2">
                <i class="bi bi-arrow-left me-2"></i>Назад
              </button>

              <button type="submit" class="btn btn-primary flex-grow-1 py-2"
                      :disabled="isLoading">
                <template v-if="isLoading">
                  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Сохранение...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-2"></i>Сохранить
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

const router = useRouter()
const route = useRoute()
const subObjectId = route.params.id

const subObject = ref({
  id: '',
  name: '',
  title: '',
  projectId: '',
})

const error = ref(null)
const isLoading = ref(false)

// Загрузка данных подобъекта
const fetchSubObject = async () => {
  isLoading.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      await router.push('/login')
      return
    }

    const response = await fetch(`http://localhost:8080/subobjects/subObject/${route.params.id}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token')
        await router.push('/login')
      }
      error.value = 'Не удалось загрузить данные подобъекта';
      return;
    }

    subObject.value = await response.json()
  } catch (err) {
    error.value = err.message
    console.error('Ошибка загрузки:', err)
  } finally {
    isLoading.value = false
  }
}

// Обновление подобъекта
const updateSubObject = async () => {
  error.value = null
  isLoading.value = true

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      error.value = 'Требуется авторизация'
      await router.push('/login')
      return
    }

    const response = await fetch(`http://localhost:8080/subobjects/${subObjectId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(subObject.value)
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при обновлении подобъекта'

      if (response.status === 401) {
        localStorage.removeItem('token')
        await router.push('/login')
      }
      return
    }

    await router.push(`/subObjects/${subObject.value.projectId}`)
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}



// Загружаем данные при монтировании компонента
onMounted(fetchSubObject)
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