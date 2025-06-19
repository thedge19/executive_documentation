<template>
  <main class="bg-light min-vh-100">
    <Navbar />

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
                       :value="4" v-model="projectId" @change="onChangeProject()">
                <label class="btn btn-outline-primary" for="project1">
                  <i class="bi bi-tree me-2"></i>Грушовая
                </label>

                <input type="radio" class="btn-check" name="project"
                       id="project2" autocomplete="off"
                       :value="5" v-model="projectId" @change="onChangeProject()">
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
                        @change="onChangeSubObject()" v-model="subObjectId">
                  <option selected disabled value="">Выберите подобъект...</option>
                  <option v-for="subObject in subObjects" :value="subObject.id">
                    {{ subObject.name }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Выбор работ -->
            <div class="mb-4">
              <label class="form-label fw-semibold">
                <i class="bi bi-hammer me-2"></i>Работы
              </label>
              <div class="d-flex gap-3 align-items-center">
                <select class="form-select" id="workSelect"
                        v-model="workId" @change="onChangeWork()">
                  <option selected disabled value="">Выберите работу...</option>
                  <option v-for="work in works" :value="work.id">
                    {{ work.name }}
                  </option>
                </select>

                <div v-if="workId" class="d-flex gap-2 align-items-center">
                  <span class="badge bg-light text-dark">{{ currentWork.units }}</span>
                  <span class="badge bg-light text-dark">{{ currentWork.finalQuantity }}</span>
                </div>
              </div>

              <div class="mt-3">
                <label class="form-label fw-semibold">
                  <i class="bi bi-123 me-2"></i>Выполненный объём
                </label>
                <input class="form-control" type="number" step="0.001"
                       v-model="workDone" placeholder="Введите количество">
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
                  <span class="badge bg-light text-dark">{{ materialInputs[n-1].units }}</span>
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
              <button @click.prevent="getSomething" class="btn btn-outline-success flex-grow-1 py-2">
                <i class="bi bi-lightning-charge me-2"></i>Проверить
              </button>

              <button type="submit" class="btn btn-primary flex-grow-1 py-2">
                <i class="bi bi-check-circle me-2"></i>Сохранить акт
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import Navbar from "@/components/Navbar.vue";

export default {
  name: "AddAct",
  components: {
    Navbar
  },
  data() {
    return {
      subObjects: [],
      works: [],
      materials: [],
      errors: [],

      currentWork: {
        units: "т",
        finalQuantity: 0.1
      },

      projectId: 4,
      subObjectId: null,
      workId: null,
      nextWorkId: null,
      workDone: "",
      startDate: new Date(),
      endDate: "",
      materialQuantity: 0,
      executiveSchema: "Нет",
      file: null,
      fileInput: null,

      // Массив для хранения данных о материалах
      materialInputs: Array(5).fill().map(() => ({
        id: null,
        units: "-",
        quantity: null
      })),

      attributes: {
        highlight: true,
        dates: this.setFirstEndDate,
      }
    }
  },
  mounted() {
    this.getSubObjects()
    this.getWorks()
    this.getMaterials()
  },
  methods: {
    checkForm(e) {
      this.errors = [];

      if (this.materialQuantity !== 0 && this.setControlDate > this.startDate) {
        this.errors.push('Дата входного контроля не должна быть позднее, чем дата начала работ.');
      }

      if (this.workDone === '') {
        this.errors.push('Заполните объём работ.');
      }

      if (this.workId === null || this.workId === undefined) {
        this.errors.push("Укажите работы.")
      }

      if (this.executiveSchema === 'Есть' && !this.file) {
        this.errors.push('Загрузите исполнительную схему (PDF файл).');
      }

      if (this.errors.length === 0) {
        this.addAct();
      }

      e.preventDefault();
    },
    getSomething() {
      console.log(this.setControlDate);
      console.log(this.startDate);
      console.log(this.endDate);
    },
    onChangeProject() {
      this.getSubObjects()
    },
    onChangeSubObject() {
      this.getWorks()
    },
    onChangeWork() {
      this.getWork()
    },
    onChangeMaterial(index) {
      const materialId = this.materialInputs[index].id;
      if (materialId) {
        fetch(`http://localhost:8080/materials/${materialId}`)
            .then(res => res.json())
            .then(data => {
              this.materialInputs[index].units = data.units;
            });
      }
    },
    getSubObjects() {
      fetch(`http://localhost:8080/subobjects/${this.projectId}`)
          .then(res => res.json())
          .then(data => {
            this.subObjects = data
          })
    },
    getWorks() {
      if (this.subObjectId) {
        fetch(`http://localhost:8080/workings/undone/${this.subObjectId}`)
            .then(res => res.json())
            .then(data => {
              this.works = data
            })
      }
    },
    getWork() {
      if (this.workId) {
        fetch(`http://localhost:8080/workings/working/${this.workId}`)
            .then(res => res.json())
            .then(data => {
              this.currentWork = data
            })
      }
    },
    getMaterials() {
      fetch(`http://localhost:8080/materials/notPageable`)
          .then(res => res.json())
          .then(data => {
            this.materials = data
          })
    },
    addMaterials() {
      return this.materialInputs
          .slice(0, this.materialQuantity)
          .filter(m => m.id && m.quantity)
          .map(m => ({
            materialId: m.id,
            quantity: m.quantity
          }));
    },
    async addAct() {
      try {
        const materials = this.addMaterials();
        const formatDate = (date) => {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        };

        const formData = new FormData();
        formData.append('projectId', this.projectId);
        formData.append('subObjectId', this.subObjectId);
        formData.append('workId', this.workId);
        formData.append('nextWorkId', this.nextWorkId);
        formData.append('workDone', parseFloat(this.workDone));
        formData.append('startDate', formatDate(this.startDate));
        formData.append('endDate', formatDate(this.endDate));
        formData.append('executiveSchema', this.executiveSchema);
        formData.append('materials', JSON.stringify(materials));

        if (this.materialQuantity > 0) {
          formData.append('controlDate', formatDate(this.setControlDate));
        }

        if (this.executiveSchema === 'Есть' && this.file) {
          formData.append('file', this.file);
        }

        await fetch('http://localhost:8080/acts', {
          method: 'POST',
          body: formData
        });

        this.$router.push("/");
      } catch (error) {
        console.error('Ошибка:', error);
        this.errors.push('Не удалось сохранить акт');
      }
    },
    handleFileUpload(event) {
      this.file = event.target.files[0];
      if (this.file && this.file.type !== 'application/pdf') {
        alert('Пожалуйста, загрузите файл в формате PDF');
        this.file = null;
        this.$refs.fileInput.value = '';
      }
    },
  },
  computed: {
    setControlDate() {
      let controlDate = new Date(this.startDate.getFullYear() + "." + (this.startDate.getMonth() + 1) + "." + 1);
      if (controlDate.getDay() === 6) {
        controlDate.setDate(controlDate.getDate() + 2);
      } else if (controlDate.getDay() === 0) {
        controlDate.setDate(controlDate.getDate() + 1);
      }
      return controlDate;
    },
    setFirstEndDate() {
      return this.startDate
    }
  }
}
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

.v-date-picker {
  width: 100%;
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