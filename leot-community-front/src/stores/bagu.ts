import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Question, QuestionBank, SearchParams, PageParams } from '@/types/bagu'
import { questionApi } from '@/api/question'
import { questionBankApi } from '@/api/questionBank'
import { questionBankQuestionApi } from '@/api/questionBankQuestion'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

export const useBaguStore = defineStore('bagu', () => {
  const userStore = useUserStore()
  // 状态
  const banks = ref<QuestionBank[]>([])
  const currentBank = ref<QuestionBank | null>(null)
  const questions = ref<Question[]>([])
  const currentQuestion = ref<Question | null>(null)
  const favourites = ref<Question[]>([])
  const loading = ref(false)
  const total = ref(0)
  
  // 题库题目导航列表（用于题目详情页左侧导航）
  const bankQuestionList = ref<Question[]>([])

  // 获取题库列表
  async function fetchBanks(params: { current: number; pageSize: number; title?: string } = { current: 1, pageSize: 20 }) {
    loading.value = true
    try {
      const res = await questionBankApi.listByPage(params)
      if (res.code === 0) {
        banks.value = res.data.records
        total.value = res.data.total
      }
    } catch (error) {
      console.error('获取题库列表失败', error)
    } finally {
      loading.value = false
    }
  }

  // 获取题库详情
  async function fetchBankDetail(id: number) {
    loading.value = true
    try {
      const res = await questionBankApi.getById(id)
      if (res.code === 0) {
        currentBank.value = res.data
      }
    } catch (error) {
      console.error('获取题库详情失败', error)
    } finally {
      loading.value = false
    }
  }

  // 获取题目列表
  async function fetchQuestions(params: { current: number; pageSize: number; questionBankId?: number }) {
    loading.value = true
    try {
      const res = await questionApi.listByPage(params)
      if (res.code === 0) {
        questions.value = res.data.records
        total.value = res.data.total
      }
    } catch (error) {
      console.error('获取题目列表失败', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索题目
  async function searchQuestions(params: SearchParams) {
    loading.value = true
    try {
      const res = await questionApi.search(params)
      if (res.code === 0) {
        questions.value = res.data.records
        total.value = res.data.total
      }
    } catch (error) {
      console.error('搜索题目失败', error)
    } finally {
      loading.value = false
    }
  }

  // 获取题目详情
  async function fetchQuestionDetail(id: number) {
    loading.value = true
    try {
      const res = await questionApi.getById(id)
      if (res.code === 0) {
        currentQuestion.value = res.data
      }
    } catch (error) {
      console.error('获取题目详情失败', error)
    } finally {
      loading.value = false
    }
  }

  // 点赞/取消点赞
  async function toggleThumb(questionId: number) {
    if (!userStore.token) {
      ElMessage.warning('请先登录')
      return
    }
    try {
      const res = await questionApi.thumb(questionId)
      if (res.code === 0) {
        // 更新本地状态
        const question = questions.value.find(q => q.id === questionId)
        if (question) {
          question.hasThumb = !question.hasThumb
          question.thumbNum += question.hasThumb ? 1 : -1
        }
        if (currentQuestion.value?.id === questionId) {
          currentQuestion.value.hasThumb = !currentQuestion.value.hasThumb
          currentQuestion.value.thumbNum += currentQuestion.value.hasThumb ? 1 : -1
        }
        ElMessage.success(res.data ? '点赞成功' : '取消点赞')
      }
    } catch (error) {
      console.error('点赞操作失败', error)
      ElMessage.error('操作失败')
    }
  }

  // 收藏/取消收藏
  async function toggleFavour(questionId: number) {
    if (!userStore.token) {
      ElMessage.warning('请先登录')
      return
    }
    try {
      const res = await questionApi.favour(questionId)
      if (res.code === 0) {
        // 更新本地状态
        const question = questions.value.find(q => q.id === questionId)
        if (question) {
          question.hasFavour = !question.hasFavour
          question.favourNum += question.hasFavour ? 1 : -1
        }
        if (currentQuestion.value?.id === questionId) {
          currentQuestion.value.hasFavour = !currentQuestion.value.hasFavour
          currentQuestion.value.favourNum += currentQuestion.value.hasFavour ? 1 : -1
        }
        ElMessage.success(res.data ? '收藏成功' : '取消收藏')
      }
    } catch (error) {
      console.error('收藏操作失败', error)
      ElMessage.error('操作失败')
    }
  }

  // 获取收藏列表
  async function fetchFavourites(params: PageParams) {
    if (!userStore.token) {
      ElMessage.warning('请先登录')
      return
    }
    loading.value = true
    try {
      const res = await questionApi.listFavour(params)
      if (res.code === 0) {
        favourites.value = res.data.records
        total.value = res.data.total
      }
    } catch (error) {
      console.error('获取收藏列表失败', error)
    } finally {
      loading.value = false
    }
  }

  // 获取题库下所有题目（用于导航列表）
  async function fetchBankQuestions(bankId: number) {
    loading.value = true
    try {
      const res = await questionBankQuestionApi.listByBankId(bankId)
      if (res.code === 0) {
        bankQuestionList.value = res.data || []
      }
    } catch (error) {
      console.error('获取题库题目列表失败', error)
      bankQuestionList.value = []
    } finally {
      loading.value = false
    }
  }

  // 获取当前题目在列表中的索引
  function getCurrentQuestionIndex(questionId: number): number {
    return bankQuestionList.value.findIndex(q => q.id === questionId)
  }

  // 获取上一题ID
  function getPrevQuestionId(currentId: number): number | null {
    const index = getCurrentQuestionIndex(currentId)
    if (index > 0) {
      return bankQuestionList.value[index - 1].id
    }
    return null
  }

  // 获取下一题ID
  function getNextQuestionId(currentId: number): number | null {
    const index = getCurrentQuestionIndex(currentId)
    if (index >= 0 && index < bankQuestionList.value.length - 1) {
      return bankQuestionList.value[index + 1].id
    }
    return null
  }

  return {
    banks,
    currentBank,
    questions,
    currentQuestion,
    favourites,
    loading,
    total,
    bankQuestionList,
    fetchBanks,
    fetchBankDetail,
    fetchQuestions,
    searchQuestions,
    fetchQuestionDetail,
    toggleThumb,
    toggleFavour,
    fetchFavourites,
    fetchBankQuestions,
    getCurrentQuestionIndex,
    getPrevQuestionId,
    getNextQuestionId
  }
})
