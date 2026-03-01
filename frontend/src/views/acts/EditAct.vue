<template>
  <Navbar/>
  <div class="container py-5">
    <div class="card shadow-sm border-0 mx-auto" style="max-width: 800px;">
      <div class="card-header bg-white py-4">
        <h2 class="h4 mb-0 text-center text-primary">Редактирование акта № {{ act.actNumber }}</h2>
      </div>

      <div class="card-body">
        <form @submit.prevent="updateAct">
          <!-- Ошибки -->
          <div v-if="errors.length" class="alert alert-danger mb-4">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            <strong>Исправьте следующие ошибки:</strong>
            <ul class="mb-0 mt-2">
              <li v-for="error in errors">{{ error }}</li>
            </ul>
          </div>

          <!-- Статус исполнительной схемы -->
          <div class="mb-4">
            <label class="form-label fw-semibold d-block mb-3">
              <i class="bi bi-file-earmark-pdf me-2"></i>Исполнительная схема
            </label>

            <div v-if="act.executiveSchemaId != null" class="alert alert-success mb-3">
              <i class="bi bi-check-circle-fill me-2"></i>
              Исполнительная схема уже добавлена.
            </div>

            <div v-else>
              <div class="btn-group w-100 mb-3" role="group">
                <input type="radio" class="btn-check" id="schemaNo"
                       value="Нет" v-model="executiveSchema">
                <label class="btn btn-outline-secondary" for="schemaNo">Нет</label>

                <input type="radio" class="btn-check" id="schemaYes"
                       value="Есть" v-model="executiveSchema">
                <label class="btn btn-outline-secondary" for="schemaYes">Есть</label>
              </div>

              <div v-if="executiveSchema === 'Есть'" class="mb-3">
                <label class="form-label">Загрузить PDF</label>
                <input type="file" class="form-control" accept=".pdf"
                       @change="handleFileUpload" ref="fileInput">
                <div class="form-text">Разрешены только файлы в формате PDF</div>
              </div>
            </div>
          </div>

          <!-- Кнопки -->
          <div class="d-flex gap-3 mt-4">
            <button type="submit" class="btn btn-primary flex-grow-1 py-2" :disabled="isLoading">
              <template v-if="isLoading">
                <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Обновление...
              </template>
              <template v-else>
                <i class="bi bi-check-circle me-2"></i>Обновить акт
              </template>
            </button>

            <button type="button" class="btn btn-outline-secondary py-2"
                    @click="router.push('/')" :disabled="isLoading">
              <i class="bi bi-arrow-left me-2"></i>Назад
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'

const route = useRoute()
const router = useRouter()

const act = ref({
  id: '',
  actNumber: null,
  // works: '',
  executiveSchemaId: null,
})

const executiveSchema = ref('Нет')
const selectedFile = ref(null)
const fileInput = ref(null)
const errors = ref([])
const isLoading = ref(false)

const getAuthHeaders = () => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (!token) {
    throw new Error('Требуется авторизация')
  }
  return {
    'Authorization': `Bearer ${token}`
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  sessionStorage.removeItem('token')
  router.push('/login')
}

const getAct = async () => {
  try {
    const headers = getAuthHeaders()
    const response = await fetch(`http://localhost:8080/acts/${route.params.id}`, {
      headers
    })

    if (!response.ok) {
      if (response.status === 401) handleUnauthorized()
      throw new Error('Ошибка загрузки акта')
    }

    const data = await response.json()
    act.value = data

    if (data.executiveSchemaId != null) {
      executiveSchema.value = 'Есть'
    }
  } catch (error) {
    console.error('Ошибка:', error)
    errors.value.push(error.message)
  }
}

const handleFileUpload = (event) => {
  selectedFile.value = event.target.files[0]
  if (selectedFile.value && selectedFile.value.type !== 'application/pdf') {
    errors.value.push('Пожалуйста, загрузите файл в формате PDF')
    selectedFile.value = null
    fileInput.value.value = ''
  }
}

const validateForm = () => {
  errors.value = []

  if (executiveSchema.value === 'Есть' && !selectedFile.value && !act.value.executiveSchemaId) {
    errors.value.push('Загрузите исполнительную схему (PDF файл).')
  }

  if (selectedFile.value && selectedFile.value.type !== 'application/pdf') {
    errors.value.push('Разрешены только файлы в формате PDF.')
  }

  return errors.value.length === 0
}

const updateAct = async (event) => {
  event.preventDefault()

  if (!validateForm()) {
    return
  }

  isLoading.value = true

  try {
    const formData = new FormData()
    if (selectedFile.value) {
      formData.append('file', selectedFile.value)
    }

    const headers = getAuthHeaders()
    delete headers['Content-Type']

    const response = await fetch(`http://localhost:8080/acts/${route.params.id}`, {
      method: 'PATCH',
      headers,
      body: formData
    })

    if (!response.ok) {
      if (response.status === 401) handleUnauthorized()
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.message || `Ошибка сервера: ${response.status}`)
    }

    await router.push('/')
  } catch (error) {
    console.error('Ошибка обновления:', error)
    errors.value.push(error.message || 'Не удалось обновить акт')
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  await getAct()
})
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.form-control, .form-select {
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

/* Стили для textarea */
textarea.form-control {
  min-height: 120px;
  resize: vertical;
}

@media (max-width: 768px) {
  .d-flex {
    flex-direction: column;
    gap: 12px;
  }

  .btn-group .btn {
    flex: 1 0 45%;
    margin-bottom: 8px;
  }
}
</style>