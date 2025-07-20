<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-primary fixed-top shadow">
    <div class="container-fluid">
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
              data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
              aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarSupportedContent">
        <ul class="navbar-nav me-auto mb-1 mb-lg-0">
          <li class="nav-item">
            <router-link class="nav-link" active-class="active" to="/">Домой</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" active-class="active" to="/projects">Объекты</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" active-class="active" to="/materials">Материалы</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" active-class="active" to="/standards">СП</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" active-class="active" to="/controls">Входняк</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" active-class="active" to="/workLog3">ОЖР 3 раздел</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" active-class="active" to="/workLog6">ОЖР 6 раздел</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" active-class="active" to="/schemas">Схемы</router-link>
          </li>
        </ul>

        <!-- Блок пользователя -->
        <div class="d-flex align-items-center ms-3">
          <span class="me-3 user-greeting">
            <a href="/dashboard" class="text-white ">{{ currentUser }}</a>
          </span>
          <button @click="logout" class="btn btn-outline-light logout-btn">
            <i class="bi bi-box-arrow-right me-1"></i> Выход
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  data() {
    return {
      currentUser: null,
      isAuthenticated: false
    }
  },
  async created() {
    await this.checkAuth()
  },
  methods: {
    async checkAuth() {
      const token = localStorage.getItem('token')
      if (token) {
        try {
          const response = await fetch('http://localhost:8080/api/auth/me', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          })

          if (response.ok) {
            const data = await response.json()
            this.currentUser = data.username
            this.isAuthenticated = true
          } else {
            this.clearAuth()
          }
        } catch (error) {
          console.error('Ошибка проверки авторизации:', error)
          this.clearAuth()
        }
      }
    },
    async logout() {
      try {
        // Вызываем endpoint для logout на сервере
        await fetch('http://localhost:8080/api/auth/logout', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
      } catch (error) {
        console.error('Ошибка при выходе:', error)
      } finally {
        this.clearAuth()
        this.$router.push('/login')
      }
    },
    clearAuth() {
      localStorage.removeItem('token')
      this.currentUser = null
      this.isAuthenticated = false
    }
  }
}
</script>

<style scoped>
.navbar {
  padding: 0.5rem 1rem;
}

.nav-link {
  color: rgba(255, 255, 255, 0.85);
  padding: 0.5rem 1rem;
  margin: 0 0.1rem;
  border-radius: 0.3rem;
  transition: all 0.2s ease;
  position: relative;
}

.nav-link:hover {
  color: white;
  background-color: rgba(255, 255, 255, 0.1);
  text-decoration: none;
}

.nav-link.active {
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  font-weight: 500;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 1rem;
  right: 1rem;
  height: 3px;
  background-color: white;
  border-radius: 3px 3px 0 0;
}

.user-greeting {
  font-size: 0.9rem;
  opacity: 0.9;
}

.logout-btn {
  padding: 0.25rem 0.75rem;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

@media (max-width: 991.98px) {
  .nav-link {
    margin: 0.2rem 0;
  }

  .nav-link.active::after {
    left: 0;
    right: 0;
    bottom: auto;
    top: 0;
    height: 100%;
    width: 3px;
    border-radius: 0 3px 3px 0;
  }

  .user-greeting {
    display: none;
  }

  .logout-btn {
    margin-top: 0.5rem;
    width: 100%;
  }
}
</style>