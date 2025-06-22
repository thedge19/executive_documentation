<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Добавить подобъект</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="addSubObject">
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

            <!-- Выбор проекта -->
            <div class="mb-4">
              <label class="form-label fw-semibold d-block mb-3">
                <i class="bi bi-diagram-2 me-2"></i>Проект
              </label>
              <div class="btn-group w-100" role="group">
                <input type="radio" class="btn-check" name="projectId"
                       id="project1" autocomplete="off"
                       :value="4" v-model="subObject.projectId">
                <label class="btn btn-outline-primary" for="project1">
                  <i class="bi bi-tree me-2"></i>Грушовая
                </label>

                <input type="radio" class="btn-check" name="projectId"
                       id="project2" autocomplete="off"
                       :value="5" v-model="subObject.projectId">
                <label class="btn btn-outline-primary" for="project2">
                  <i class="bi bi-building me-2"></i>Шесхарис
                </label>
              </div>
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопки -->
            <div class="d-flex gap-3">
              <button @click.prevent="getSomething"
                      class="btn btn-outline-success flex-grow-1 py-2"
                      :disabled="isLoading">
                <i class="bi bi-lightning-charge me-2"></i>Проверить
              </button>

              <button type="submit" class="btn btn-primary flex-grow-1 py-2"
                      :disabled="isLoading">
                <template v-if="isLoading">
                  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Добавление...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-2"></i>Добавить
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
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const subObject = ref({
  name: '',
  title: '',
  projectId: 4
})
const error = ref(null)
const isLoading = ref(false)

const getSomething = () => {
  console.log('Выбран проект ID:', subObject.value)
}

const addSubObject = async () => {
  error.value = null
  isLoading.value = true

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      error.value = 'Требуется авторизация'
      await router.push('/login')
      return
    }

    const response = await fetch('http://localhost:8080/subobjects', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(subObject.value)
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при добавлении подобъекта'

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