<template>
  <div class="login-container">
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="username">Username</label>
        <input
            type="text"
            id="username"
            v-model="username"
            required
        />
      </div>
      <div class="form-group">
        <label for="password">Password</label>
        <input
            type="password"
            id="password"
            v-model="password"
            required
        />
      </div>
      <button type="submit">Login</button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: '',
      error: ''
    };
  },
  methods: {
    async handleLogin() {
      try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: this.username,
            password: this.password
          })
        })

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}))
          this.error = errorData.message || 'Login failed'
          return
        }

        const data = await response.json()
        console.log('Login response:', data) // Добавьте эту строку для отладки

        // Проверьте имя поля с токеном (может быть 'accessToken' вместо 'token')
        const token = data.token || data.accessToken
        if (!token) {
          throw new Error('Token not found in response')
        }

        localStorage.setItem('token', token)
        this.$router.push('/')
      } catch (err) {
        this.error = err.message || 'Invalid username or password'
        console.error('Login error:', err)
      }
    }
  }
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  margin: 50px auto 0;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
}

input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

button {
  background-color: #4CAF50;
  color: white;
  padding: 10px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #45a049;
}

.error {
  color: red;
  margin-top: 10px;
}
</style>