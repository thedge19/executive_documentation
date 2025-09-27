<template>
  <div class="page-wrapper">
    <Navbar/>
    <div class="container-fluid px-3 py-2">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">

        <div class="card-body p-3 mt-5">
          <form @submit.prevent="addMaterial">
            <!-- Наименование -->
            <div class="mb-3">
              <label for="name" class="form-label fw-semibold small">
                <i class="bi bi-tag me-1"></i>Наименование материала
              </label>
              <input id="name" type="text" class="form-control form-control-sm"
                     placeholder="Введите наименование материала"
                     required v-model="material.name">
            </div>

            <!-- Единицы измерения -->
            <div class="mb-3">
              <label for="units" class="form-label fw-semibold small">
                <i class="bi bi-rulers me-1"></i>Ед. изм.
              </label>
              <input id="units" type="text" class="form-control form-control-sm"
                     placeholder="Введите единицы измерения"
                     required v-model="material.units">
            </div>

            <!-- Данные сертификата -->
            <div class="mb-3">
              <label class="form-label fw-semibold small">
                <i class="bi bi-file-earmark-text me-1"></i>Данные сертификата
              </label>

              <!-- Тип документа -->
              <div class="mb-2">
                <label class="form-label small">Тип документа</label>
                <select class="form-select form-select-sm" v-model="material.certificateType">
                  <option value="" disabled selected>Выберите тип</option>
                  <option v-for="type in documentTypes" :value="type">{{ type }}</option>
                </select>
              </div>

              <!-- Номер и дата -->
              <div class="row g-2 mb-2">
                <div class="col-md-6">
                  <label class="form-label small">Номер документа</label>
                  <input type="text" class="form-control form-control-sm" placeholder="Номер" v-model="material.certificateNumber">
                </div>
                <div class="col-md-6">
                  <label class="form-label small">Дата документа</label>
                  <input type="date" class="form-control form-control-sm" v-model="material.certificateDate">
                </div>
              </div>

              <!-- Автор сертификата -->
              <div class="mb-2">
                <label class="form-label small">Автор сертификата</label>
                <input type="text" class="form-control form-control-sm" placeholder="Введите автора сертификата" v-model="material.author">
              </div>
            </div>

            <!-- ГОСТ, ТУ -->
            <div class="mb-3">
              <label for="standard" class="form-label fw-semibold small">
                <i class="bi bi-file-earmark-check me-1"></i>ГОСТ, ТУ
              </label>
              <input id="standard" type="text" class="form-control form-control-sm"
                     placeholder="Введите ГОСТ или ТУ"
                     required v-model="material.standard">
            </div>

            <!-- Загрузка файла -->
            <div class="mb-3">
              <label class="form-label fw-semibold small">
                <i class="bi bi-file-earmark-pdf me-1"></i>Файл сертификата (PDF)
              </label>
              <input @change="handleFileUpload" class="form-control form-control-sm"
                     type="file" accept=".pdf">
              <small class="text-muted small" v-if="file">Выбран файл: {{ file.name }}</small>

              <div v-if="uploadProgress > 0 && uploadProgress < 100" class="mt-1">
                <div class="progress" style="height: 20px;">
                  <div class="progress-bar progress-bar-striped progress-bar-animated"
                       :style="{ width: uploadProgress + '%' }">
                    {{ uploadProgress }}%
                  </div>
                </div>
              </div>

              <div v-if="uploadError" class="alert alert-danger mt-1 py-1 small">
                <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ uploadError }}
              </div>
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-3 py-1 small">
              <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ error }}
            </div>

            <!-- Кнопка отправки -->
            <div class="d-grid">
              <button type="submit" class="btn btn-primary btn-sm" :disabled="isUploading">
                <template v-if="isUploading">
                  <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                  Загрузка...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-1"></i>Сохранить материал
                </template>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import Navbar from '../../components/Navbar.vue';

const router = useRouter();

const documentTypes = ref([
  'Декларация о соответствии',
  'Информационное письмо',
  'Паспорт изделия',
  'Письмо',
  'Свидетельство о государственной регистрации',
  'Сертификат качества',
  'Сертификат соответствия',
]);

const material = ref({
  name: '',
  units: '',
  standard: '',
  author: '',
  certificateType: '',
  certificateNumber: '',
  certificateDate: '',
});

const file = ref(null);
const isUploading = ref(false);
const uploadProgress = ref(0);
const uploadError = ref(null);
const error = ref(null);

const handleFileUpload = (event) => {
  const selectedFile = event.target.files[0];
  if (!selectedFile) {
    file.value = null;
    return;
  }

  // Валидация файла
  if (selectedFile.type !== 'application/pdf') {
    uploadError.value = 'Пожалуйста, загрузите файл в формате PDF';
    file.value = null;
    return;
  }

  file.value = selectedFile;
  uploadError.value = null;
};

const formatDateForDisplay = (isoDate) => {
  if (!isoDate) return '';
  const [year, month, day] = isoDate.split('-');
  return `${day}.${month}.${year}`;
};

const generateCertificateName = () => {
  const type = material.value.certificateType;
  const number = material.value.certificateNumber;
  const date = material.value.certificateDate;

  if (type && number && date) {
    return `${type} №${number} от ${formatDateForDisplay(date)} г.`;
  }
  return '';
};

const addMaterial = async () => {
  try {
    isUploading.value = true;
    error.value = null;
    uploadError.value = null;

    const token = localStorage.getItem('token')
    if (!token) {
      error.value = 'Токен отсутствует';
      return;
    }

    if (!file.value) {
      uploadError.value = 'Пожалуйста, загрузите файл сертификата';
      return;
    }

    const formData = new FormData();

    // Формируем имя сертификата из данных формы
    const certificateName = generateCertificateName();

    // Создаем DTO для материала
    const materialDto = {
      name: material.value.name,
      units: material.value.units,
      standard: material.value.standard,
      author: material.value.author,
      certificateName: certificateName,
    };

    // Добавляем material как JSON
    formData.append('material', new Blob([JSON.stringify(materialDto)], {
      type: 'application/json'
    }));

    // Добавляем файл
    formData.append('file', file.value);

    const response = await fetch('http://localhost:8080/materials', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      error.value = errorData.message || `Ошибка ${response.status}: ${response.statusText}`;
      return;
    }

    await router.push("/materials");
  } catch (err) {
    error.value = err.message || 'Произошла ошибка при сохранении';
    console.error('Error:', err);
  } finally {
    isUploading.value = false;
    uploadProgress.value = 0;
  }
};
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container-fluid {
  flex: 1;
  display: flex;
  align-items: flex-start;
  min-height: 0;
}

.card {
  border-radius: 8px;
  overflow: hidden;
  max-height: calc(100vh - 80px);
  overflow-y: auto;
}

.card-body {
  max-height: none;
}

.form-control, .form-select {
  border-radius: 6px;
  padding: 6px 10px;
}

.form-label {
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.btn {
  border-radius: 6px;
  transition: all 0.2s;
  padding: 6px 12px;
}

.alert {
  border-radius: 6px;
  margin-bottom: 8px;
}

.progress {
  border-radius: 6px;
}

/* Убираем скролл у карточки на очень маленьких экранах */
@media (max-width: 576px) {
  .container-fluid {
    padding-left: 5px;
    padding-right: 5px;
  }

  .card {
    border-radius: 4px;
    max-height: calc(100vh - 70px);
  }
}

/* Гарантируем, что все элементы помещаются */
@media (max-height: 700px) {
  .card-body {
    padding: 1rem;
  }

  .mb-3 {
    margin-bottom: 0.5rem !important;
  }

  .mb-2 {
    margin-bottom: 0.25rem !important;
  }
}
</style>