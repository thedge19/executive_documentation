<template>
    <main>
        <Navbar/>
        <div class="my-5">
            <div class="mx-auto w-25 " style="max-width:100%;">
                <h2 class="text-center mb-3">Добавить СП</h2>
                <form @submit.prevent="addStandard">
                    <!--name-->
                    <div class="row">
                        <div class="col-md-12 form-group mb-3">
                            <label for="name" class="form-label">Наименование</label>
                            <input id="name" type="text" name="name" class="form-control" placeholder="наименование"
                                   required v-model="standard.name">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-12 form-group">
                            <input class="btn btn-primary w-100" type="submit" value="Submit">
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
    name: 'AddStandard',
    components: {
        Navbar
    },

    data() {
        return {
            standard: {
                name: '',
            }
        }
    },

    methods: {
        addStandard() {
            fetch('http://localhost:8080/standards', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(this.standard)
            })
                .then(data => {
                    console.log(data)
                    this.$router.push("/standards");
                })
        }
    },

}


</script>