<template>
  <div class="card">
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="ALL">全部</el-radio-button>
        <el-radio-button value="ON_SHELF">上架</el-radio-button>
        <el-radio-button value="OFF_SHELF">下架</el-radio-button>
        <el-radio-button value="DRAFT">草稿</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="openEdit()">+ 新增商品</el-button>
    </div>

    <el-table :data="products" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="商品名称" min-width="160" />
      <el-table-column label="售价" width="100">
        <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="{ ON_SHELF: 'success', OFF_SHELF: 'info', DRAFT: 'warning' }[row.status]">
            {{ { ON_SHELF: '上架', OFF_SHELF: '下架', DRAFT: '草稿' }[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 'ON_SHELF' ? 'info' : 'success'" @click="toggle(row)">
            {{ row.status === 'ON_SHELF' ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialog" :title="form.id ? '编辑商品' : '新增商品'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类（可选）" clearable style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="售价" required><el-input-number v-model="form.price" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="库存" required><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="主图">
          <div class="cover-upload">
            <img v-if="form.cover" :src="form.cover" class="cover-img" />
            <label class="up-btn">
              {{ uploading ? '上传中…' : '选择图片' }}
              <input type="file" accept="image/*" hidden @change="onCover" :disabled="uploading" />
            </label>
          </div>
          <el-input v-model="form.cover" placeholder="或直接粘贴图片 URL" style="margin-top: 6px" />
        </el-form-item>
        <el-form-item label="详情"><el-input v-model="form.detail" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="ON_SHELF">上架</el-radio>
            <el-radio value="OFF_SHELF">下架</el-radio>
            <el-radio value="DRAFT">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myProducts, createProduct, updateProduct, changeProductStatus, deleteProduct, myCategories, uploadFile } from '../../api'

const uploading = ref(false)

async function onCover(e) {
  const f = e.target.files[0]
  if (!f) return
  uploading.value = true
  try {
    const r = await uploadFile(f, 'PRODUCT')
    form.cover = r.url
    ElMessage.success('封面上传成功')
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

const products = ref([])
const categories = ref([])
const status = ref('ALL')
const loading = ref(false)
const dialog = ref(false)
const form = reactive({ id: null, name: '', categoryId: null, price: 1, stock: 0, cover: '', detail: '', status: 'DRAFT' })

async function load() {
  loading.value = true
  try {
    const page = await myProducts({ status: status.value, page: 0, size: 50 })
    products.value = page.content || []
  } finally { loading.value = false }
}
function openEdit(row) {
  Object.assign(form, row || { id: null, name: '', categoryId: null, price: 1, stock: 0, cover: '', detail: '', status: 'DRAFT' })
  dialog.value = true
}
async function save() {
  if (!form.name) return ElMessage.warning('请填写商品名称')
  if (form.id) await updateProduct(form.id, form)
  else await createProduct(form)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function toggle(row) {
  await changeProductStatus(row.id, row.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF')
  ElMessage.success('已更新状态')
  load()
}
async function del(row) {
  await ElMessageBox.confirm('确认删除该商品？', '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('已删除')
  load()
}
onMounted(async () => {
  load()
  categories.value = await myCategories()
})
</script>

<style scoped>
.card { background: #fff; border-radius: 10px; padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.price { color: #e8611d; font-weight: 600; }
.cover-upload { display: flex; align-items: center; gap: 10px; }
.cover-img { width: 64px; height: 64px; object-fit: cover; border-radius: 8px; }
.up-btn { display: inline-block; padding: 6px 14px; border: 1px dashed #dcdfe6; border-radius: 6px; cursor: pointer; font-size: 13px; color: #666; }
.up-btn:hover { color: #e8611d; border-color: #e8611d; }
</style>
