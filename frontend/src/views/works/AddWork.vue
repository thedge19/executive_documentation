<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Добавить работы</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="addWork">
            <!-- Подобъект -->
            <div class="input-group mb-4">
              <span class="input-group-text bg-light fw-semibold">
                <i class="bi bi-building me-2"></i>Подобъект
              </span>
              <input type="text" class="form-control" :value="subObject.name" readonly>
            </div>

            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-card-text me-2"></i>Наименование
              </label>
              <input id="name" type="text" class="form-control"
                     placeholder="Введите наименование работы"
                     required v-model="work.name">
            </div>

            <!-- Единицы измерения -->
            <div class="mb-4">
              <label for="units" class="form-label fw-semibold">
                <i class="bi bi-rulers me-2"></i>Ед. изм.
              </label>
              <input id="units" type="text" class="form-control"
                     placeholder="Введите единицы измерения"
                     required v-model="work.units">
            </div>

            <!-- Количество -->
            <div class="mb-4">
              <label for="quantity" class="form-label fw-semibold">
                <i class="bi bi-123 me-2"></i>Количество
              </label>
              <input id="quantity" type="number" step="0.001" class="form-control"
                     placeholder="Введите количество"
                     required v-model="work.quantity">
            </div>

            <!-- Стандарт -->
            <div class="mb-4">
              <label class="form-label fw-semibold">
                <i class="bi bi-file-earmark-text me-2"></i>Стандарт
              </label>
              <select class="form-select" v-model="work.standardId" required>
                <option value="" selected disabled>Выберите стандарт...</option>
                <option v-for="standard in standards" :value="standard.id">
                  {{ standard.name }}
                </option>
              </select>
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопка отправки -->
            <div class="d-grid">
              <button type="submit" class="btn btn-primary py-2">
                <i class="bi bi-check-circle me-2"></i>Добавить работу
              </button>
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
  name: 'AddWork',
  components: {
    Navbar
  },
  data() {
    return {
      projectId: 1,
      error: '',
      subObject: "",
      standards: [],
      work: {
        name: '',
        units: '',
        quantity: '',
        done: 0,
        standardId: '',
        subObjectId: this.$route.params.id
      }
    }
  },
  mounted() {
    this.getSubObject(this.$route.params.id);
    this.getStandards();
  },
  methods: {
    addWork() {
      if (!this.work.standardId) {
        this.error = 'Пожалуйста, выберите стандарт';
        return;
      }

      this.error = '';
      fetch('http://localhost:8080/workings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.work)
      })
          .then(() => {
            this.$router.push(`/works/${this.$route.params.id}`);
          })
          .catch(error => {
            console.error('Ошибка:', error);
            this.error = 'Произошла ошибка при добавлении работы';
          });
    },
    getSubObject() {
      fetch(`http://localhost:8080/subobjects/subObject/${this.$route.params.id}`)
          .then(res => res.json())
          .then(data => {
            this.subObject = data;
          })
          .catch(console.error);
    },
    getStandards() {
      fetch(`http://localhost:8080/standards`)
          .then(res => res.json())
          .then(data => {
            this.standards = data;
          })
          .catch(console.error);
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

.input-group-text {
  border-radius: 8px 0 0 8px;
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