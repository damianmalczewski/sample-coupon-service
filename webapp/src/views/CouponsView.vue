<script setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { createCoupon, describeError, listCoupons } from '../api.js';

const PAGE_SIZE = 20;

const form = reactive({ code: '', usageLimit: 1, countryCode: '' });
const creating = ref(false);
const createMessage = ref(null);

const filter = ref('');
const page = ref(0);
const coupons = ref({ content: [], page: 0, size: PAGE_SIZE, totalElements: 0 });
const loading = ref(false);
const listError = ref(null);

const totalPages = () => Math.max(1, Math.ceil(coupons.value.totalElements / PAGE_SIZE));

async function load() {
  loading.value = true;
  listError.value = null;
  try {
    coupons.value = await listCoupons({
      page: page.value,
      size: PAGE_SIZE,
      code: filter.value.trim(),
    });
  } catch (error) {
    listError.value = describeError(error);
  } finally {
    loading.value = false;
  }
}

async function submit() {
  creating.value = true;
  createMessage.value = null;
  try {
    await createCoupon({ ...form });
    createMessage.value = { ok: true, text: `Coupon ${form.code.trim().toUpperCase()} created` };
    Object.assign(form, { code: '', usageLimit: 1, countryCode: '' });
    page.value = 0;
    await load();
  } catch (error) {
    createMessage.value = { ok: false, text: describeError(error) };
  } finally {
    creating.value = false;
  }
}

let filterTimer;
watch(filter, () => {
  clearTimeout(filterTimer);
  filterTimer = setTimeout(() => {
    page.value = 0;
    load();
  }, 300);
});

function goTo(target) {
  page.value = target;
  load();
}

onMounted(load);
</script>

<template>
  <section class="panel">
    <h2>New coupon</h2>
    <form class="row" @submit.prevent="submit">
      <input
        v-model="form.code"
        placeholder="Code"
        required
        maxlength="255"
        pattern="[a-zA-Z0-9]+"
      />
      <input
        v-model.number="form.usageLimit"
        type="number"
        min="1"
        placeholder="Usage limit"
        required
      />
      <input
        v-model="form.countryCode"
        placeholder="Country"
        required
        maxlength="8"
        pattern="[a-zA-Z]+"
      />
      <button :disabled="creating">Create</button>
    </form>
    <p v-if="createMessage" :class="createMessage.ok ? 'ok' : 'error'">{{ createMessage.text }}</p>
  </section>

  <section>
    <div class="row between">
      <h2>Coupons</h2>
      <input v-model="filter" class="filter" placeholder="Filter by code" />
    </div>
    <p v-if="listError" class="error">{{ listError }}</p>
    <table v-else>
      <thead>
        <tr>
          <th>Code</th>
          <th class="num">Used</th>
          <th>Created</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="coupon in coupons.content" :key="coupon.id">
          <td class="mono">{{ coupon.code }}</td>
          <td class="num">{{ coupon.usageCount }} / {{ coupon.usageLimit }}</td>
          <td class="muted">{{ new Date(coupon.createdAt).toLocaleString() }}</td>
          <td class="num">
            <RouterLink :to="{ path: '/redeem', query: { code: coupon.code } }">Redeem</RouterLink>
          </td>
        </tr>
        <tr v-if="!loading && coupons.content.length === 0">
          <td colspan="4" class="muted">No coupons</td>
        </tr>
      </tbody>
    </table>
    <div class="row pager">
      <button :disabled="loading || page === 0" @click="goTo(page - 1)">Previous</button>
      <span class="muted">Page {{ page + 1 }} of {{ totalPages() }}</span>
      <button :disabled="loading || page + 1 >= totalPages()" @click="goTo(page + 1)">Next</button>
    </div>
  </section>
</template>
