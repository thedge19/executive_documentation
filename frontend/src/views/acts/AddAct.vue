<template>
  <Navbar/>
  <div class="container py-5">
    <div class="card shadow-sm border-0 mx-auto" style="max-width: 800px;">
      <div class="card-header bg-white py-4">
        <h2 class="h4 mb-0 text-center text-primary">Добавить акт выполненных работ</h2>
      </div>

      <div class="card-body">
        <form @submit.prevent="checkForm">
          <!-- Ошибки -->
          <div v-if="errors.length" class="alert alert-danger mb-4">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            <strong>Исправьте следующие ошибки:</strong>
            <ul class="mb-0 mt-2">
              <li v-for="error in errors">{{ error }}</li>
            </ul>
          </div>

          <!-- Выбор объекта -->
          <div class="mb-4">
            <label class="form-label fw-semibold d-block mb-3">
              <i class="bi bi-building me-2"></i>Объект
            </label>
            <div class="btn-group w-100" role="group">
              <input type="radio" class="btn-check" name="project"
                     id="project1" autocomplete="off"
                     :value="4" v-model="projectId" @change="onChangeProject">
              <label class="btn btn-outline-primary" for="project1">
                <i class="bi bi-tree me-2"></i>Грушовая
              </label>

              <input type="radio" class="btn-check" name="project"
                     id="project2" autocomplete="off"
                     :value="5" v-model="projectId" @change="onChangeProject">
              <label class="btn btn-outline-primary" for="project2">
                <i class="bi bi-building me-2"></i>Шесхарис
              </label>
            </div>
          </div>

          <!-- Выбор подобъекта -->
          <div class="mb-4">
            <label for="subObjectSelect" class="form-label fw-semibold">
              <i class="bi bi-diagram-3 me-2"></i>Подобъект
            </label>
            <div class="input-group">
                <span class="input-group-text bg-light">
                  <i class="bi bi-list-ul"></i>
                </span>
              <select class="form-select" id="subObjectSelect"
                      @change="onChangeSubObject" v-model="subObjectId">
                <option selected disabled value="">Выберите подобъект...</option>
                <option v-for="subObject in subObjects" :value="subObject.id">
                  {{ subObject.name }}
                </option>
              </select>
            </div>
          </div>

          <!-- Выбор работ -->
          <div class="mb-4">
            <div class="row align-items-end mb-2">
              <!-- Заголовок для select -->
              <div class="col-md-8">
                <label class="form-label fw-semibold">
                  <i class="bi bi-hammer me-2"></i>Работы
                </label>
              </div>

              <!-- Заголовок для input -->
              <div class="col-md-4">
                <label class="form-label fw-semibold">
                  <i class="bi bi-123 me-2"></i>Выполненный объём
                </label>
              </div>
            </div>

            <div class="row align-items-center">
              <!-- Select для работ (широкий) -->
              <div class="col-md-8 mb-2 mb-md-0">
                <select class="form-select" id="workSelect"
                        v-model="workId" @change="onChangeWork">
                  <option selected disabled value="">Выберите работу...</option>
                  <option v-for="work in works" :value="work.id">
                    {{ work.name }}
                  </option>
                </select>
              </div>

              <!-- Input для объема (узкий) + единицы измерения -->
              <div class="col-md-4 d-flex align-items-center gap-2">
                <input class="form-control" type="number" step="0.001"
                       v-model="workDone"
                       :placeholder="currentWork?.finalQuantity || 'Введите количество'"
                       style="flex: 1;">
                <span class="badge bg-light text-dark" style="white-space: nowrap;">
        {{ currentWork?.units || '' }}
      </span>
              </div>
            </div>
          </div>

          <!-- Даты -->
          <div class="mb-4">
            <label class="form-label fw-semibold">
              <i class="bi bi-calendar-range me-2"></i>Даты работ
            </label>
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Начало работ</label>
                <VDatePicker class="form-control"
                             :attributes="attributes"
                             v-model="startDate"
                             mode="date"/>
              </div>
              <div class="col-md-6">
                <label class="form-label">Окончание работ</label>
                <VDatePicker class="form-control"
                             :attributes="attributes"
                             v-model="endDate"
                             :model-value="setFirstEndDate"
                             mode="date"/>
              </div>
            </div>
          </div>

          <!-- Материалы -->
          <div class="mb-4">
            <label class="form-label fw-semibold">
              <i class="bi bi-box-seam me-2"></i>Материалы
            </label>
            <div class="mb-3">
              <label class="form-label">Количество применённых материалов</label>
              <div class="btn-group w-100" role="group">
                <input type="radio" class="btn-check" id="mat0" value="0" v-model="materialQuantity">
                <label class="btn btn-outline-secondary" for="mat0">0</label>

                <input type="radio" class="btn-check" id="mat1" value="1" v-model="materialQuantity">
                <label class="btn btn-outline-secondary" for="mat1">1</label>

                <input type="radio" class="btn-check" id="mat2" value="2" v-model="materialQuantity">
                <label class="btn btn-outline-secondary" for="mat2">2</label>

                <input type="radio" class="btn-check" id="mat3" value="3" v-model="materialQuantity">
                <label class="btn btn-outline-secondary" for="mat3">3</label>

                <input type="radio" class="btn-check" id="mat4" value="4" v-model="materialQuantity">
                <label class="btn btn-outline-secondary" for="mat4">4</label>

                <input type="radio" class="btn-check" id="mat5" value="5" v-model="materialQuantity">
                <label class="btn btn-outline-secondary" for="mat5">5</label>
              </div>
            </div>

            <div v-if="materialQuantity > 0" class="mb-3">
              <div class="alert alert-info">
                <i class="bi bi-info-circle me-2"></i>
                Дата входного контроля: {{ setControlDate.toLocaleDateString() }}
              </div>
            </div>

            <!-- Динамические поля для материалов -->
            <div v-for="n in parseInt(materialQuantity)" :key="n" class="mb-3">
              <div class="d-flex gap-3 align-items-center">
                <select class="form-select" v-model="materialInputs[n-1].id"
                        @change="onChangeMaterial(n-1)">
                  <option selected disabled value="">Выберите материал...</option>
                  <option v-for="material in materials" :value="material.id">
                    {{ material.name }}
                  </option>
                </select>
                <span class="badge bg-light text-dark">{{ materialInputs[n - 1].units }}</span>
                <input class="form-control" type="number" step="0.001"
                       v-model="materialInputs[n-1].quantity" placeholder="Количество">
              </div>
            </div>
          </div>

          <!-- Исполнительная схема -->
          <div class="mb-4">
            <label class="form-label fw-semibold">
              <i class="bi bi-file-earmark-pdf me-2"></i>Исполнительная схема
            </label>
            <div class="btn-group w-100 mb-3" role="group">
              <input type="radio" class="btn-check" id="schemaNo" value="Нет" v-model="executiveSchema">
              <label class="btn btn-outline-secondary" for="schemaNo">Нет</label>

              <input type="radio" class="btn-check" id="schemaYes" value="Есть" v-model="executiveSchema">
              <label class="btn btn-outline-secondary" for="schemaYes">Есть</label>
            </div>

            <div v-if="executiveSchema === 'Есть'">
              <label class="form-label">Загрузить PDF</label>
              <input type="file" class="form-control" accept=".pdf"
                     @change="handleFileUpload" ref="fileInput">
            </div>
          </div>

          <!-- Следующие работы -->
          <div class="mb-4">
            <label for="nextWorkSelect" class="form-label fw-semibold">
              <i class="bi bi-arrow-right-circle me-2"></i>Следующие работы
            </label>
            <select class="form-select" id="nextWorkSelect" v-model="nextWorkId">
              <option selected disabled value="">Выберите следующую работу...</option>
              <option v-for="work in works" :value="work.id">
                {{ work.name }}
              </option>
            </select>
          </div>

          <!-- Кнопки -->
          <div class="d-flex gap-3 mt-4">
            <button type="submit" class="btn btn-primary flex-grow-1 py-2" :disabled="isLoading">
              <template v-if="isLoading">
                <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Сохранение...
              </template>
              <template v-else>
                <i class="bi bi-check-circle me-2"></i>Сохранить акт
              </template>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import Navbar from '@/components/Navbar.vue'

const router = useRouter()

// Состояние компонента
const subObjects = ref([])
const works = ref([])
const materials = ref([])
const errors = ref([])
const isLoading = ref(false)
const error = ref('')

const currentWork = ref({
  units: "т",
  finalQuantity: 0.1
})

const projectId = ref(4)
const subObjectId = ref(null)
const workId = ref(null)
const nextWorkId = ref(null)
const workDone = ref("")
const startDate = ref(new Date())
const endDate = ref("")
const materialQuantity = ref(0)
const executiveSchema = ref("Нет")
const file = ref(null)
const fileInput = ref(null)

const materialInputs = ref(
    Array(5).fill().map(() => ({
      id: null,
      units: "-",
      quantity: null
    }))
)

const attributes = computed(() => ({
  highlight: true,
  dates: setFirstEndDate.value,
}))

// Вычисляемые свойства
const setControlDate = computed(() => {
  let controlDate = new Date(
      startDate.value.getFullYear() + "." +
      (startDate.value.getMonth() + 1) + "." + 1
  )
  if (controlDate.getDay() === 6) {
    controlDate.setDate(controlDate.getDate() + 2)
  } else if (controlDate.getDay() === 0) {
    controlDate.setDate(controlDate.getDate() + 1)
  }
  return controlDate
})

const setFirstEndDate = computed(() => startDate.value)

// Методы для работы с авторизацией
const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('Требуется авторизация')
  }
  return {
    'Authorization': `Bearer ${token}`
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  router.push('/login')
}

// Методы для работы с данными
const onChangeProject = async () => {
  await getSubObjects()
}

const onChangeSubObject = async () => {
  await getWorks()
}

const onChangeWork = async () => {
  await getWork()
}

const onChangeMaterial = async (index) => {
  const materialId = materialInputs.value[index].id
  if (materialId) {
    try {
      const headers = getAuthHeaders()
      const response = await fetch(`http://localhost:8080/materials/${materialId}`, {headers})

      if (!response.ok) {
        if (response.status === 401) handleUnauthorized()
        error.value = 'Ошибка загрузки материала';
        return;
      }

      const data = await response.json()
      materialInputs.value[index].units = data.units
    } catch (err) {
      console.error('Ошибка:', err)
      if (err.message.includes('авторизация')) handleUnauthorized()
    }
  }
}

const getSubObjects = async () => {
  try {
    const headers = getAuthHeaders()
    const response = await fetch(`http://localhost:8080/subobjects/${projectId.value}`, {headers})

    if (!response.ok) {
      if (response.status === 401) handleUnauthorized()
      error.value = 'Ошибка загрузки подобъектов';
      return;
    }

    subObjects.value = await response.json()
  } catch (err) {
    console.error('Ошибка:', err)
    if (err.message.includes('авторизация')) handleUnauthorized()
  }
}

const getWorks = async () => {
  if (subObjectId.value) {
    try {
      const headers = getAuthHeaders()
      const response = await fetch(`http://localhost:8080/workings/undone/${subObjectId.value}`, {headers})

      if (!response.ok) {
        if (response.status === 401) handleUnauthorized()
        error.value = 'Ошибка загрузки работ';
      }

      works.value = await response.json()
    } catch (err) {
      console.error('Ошибка:', err)
      if (err.message.includes('авторизация')) handleUnauthorized()
    }
  }
}

const getWork = async () => {
  if (workId.value) {
    try {
      const headers = getAuthHeaders()
      const response = await fetch(`http://localhost:8080/workings/working/${workId.value}`, {headers})

      if (!response.ok) {
        if (response.status === 401) handleUnauthorized()
        error.value = 'Ошибка загрузки работы';
        return;
      }

      currentWork.value = await response.json()
    } catch (err) {
      console.error('Ошибка:', err)
      if (err.message.includes('авторизация')) handleUnauthorized()
    }
  }
}

const getMaterials = async () => {
  try {
    const headers = getAuthHeaders()
    const response = await fetch('http://localhost:8080/materials', {headers})

    if (!response.ok) {
      if (response.status === 401) handleUnauthorized()
      error.value = 'Ошибка загрузки материалов';
      return;
    }

    materials.value = await response.json()
  } catch (err) {
    console.error('Ошибка:', err)
    if (err.message.includes('авторизация')) handleUnauthorized()
  }
}

const addMaterials = () => {
  return materialInputs.value
      .slice(0, materialQuantity.value)
      .filter(m => m.id && m.quantity)
      .map(m => ({
        materialId: m.id,
        quantity: m.quantity
      }))
}

const handleFileUpload = (event) => {
  file.value = event.target.files[0]
  if (file.value && file.value.type !== 'application/pdf') {
    alert('Пожалуйста, загрузите файл в формате PDF')
    file.value = null
    fileInput.value.value = ''
  }
}

const checkForm = async (e) => {
  e.preventDefault()
  errors.value = []

  if (materialQuantity.value !== 0 && setControlDate.value > startDate.value) {
    errors.value.push('Дата входного контроля не должна быть позднее, чем дата начала работ.')
  }

  if (workDone.value === '') {
    errors.value.push('Заполните объём работ.')
  }

  if (!workId.value) {
    errors.value.push("Укажите работы.")
  }

  if (executiveSchema.value === 'Есть' && !file.value) {
    errors.value.push('Загрузите исполнительную схему (PDF файл).')
  }

  if (errors.value.length === 0) {
    await addAct()
  }
}

const addAct = async () => {
  try {
    isLoading.value = true
    const materialsData = addMaterials()

    const formatDate = (date) => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    const formData = new FormData()
    formData.append('projectId', projectId.value)
    formData.append('subObjectId', subObjectId.value)
    formData.append('workId', workId.value)
    formData.append('nextWorkId', nextWorkId.value)
    formData.append('workDone', parseFloat(workDone.value))
    formData.append('startDate', formatDate(startDate.value))
    formData.append('endDate', formatDate(endDate.value))
    formData.append('executiveSchema', executiveSchema.value)
    formData.append('materials', JSON.stringify(materialsData))

    if (materialQuantity.value > 0) {
      formData.append('controlDate', formatDate(setControlDate.value))
    }

    if (executiveSchema.value === 'Есть' && file.value) {
      formData.append('file', file.value)
    }

    const headers = getAuthHeaders()
    delete headers['Content-Type'] // Для FormData заголовок Content-Type устанавливается автоматически

    const response = await fetch('http://localhost:8080/acts', {
      method: 'POST',
      headers,
      body: formData
    })

    if (!response.ok) {
      if (response.status === 401) handleUnauthorized()
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при сохранении акта';
      return;
    }

    await router.push("/")
  } catch (err) {
    console.error('Ошибка:', err)
    errors.value.push(err.message || 'Не удалось сохранить акт')
  } finally {
    isLoading.value = false
  }
}

// Инициализация компонента
onMounted(async () => {
  await getSubObjects()
  await getMaterials()
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

.input-group-text {
  border-radius: 8px 0 0 8px;
}

.badge {
  border-radius: 8px;
  padding: 8px 12px;
  font-weight: normal;
}

@media (max-width: 768px) {
  .d-flex {
    flex-direction: column;
    gap: 12px;
  }

  .btn-group {
    flex-wrap: wrap;
  }

  .btn-group .btn {
    flex: 1 0 45%;
    margin-bottom: 8px;
  }
}
</style>