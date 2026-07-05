import { computed, reactive, ref } from 'vue';
import { reviewService, type CreateReviewPayload } from '@/services/ReviewService';
import { useNotifications } from '@/composables/useNotifications';
import { useReviewersStore } from '@/stores/reviewers';
import type { Reviewer, Review } from '@/models';
import type { PaginationLinks } from '@/models/Http';

export interface ReviewEntry {
  id: number;
  rating: number;
  description: string;
  createdAt?: string;
  reviewer?: Reviewer;
  reviewerUri?: string;
  reviewerId?: number | null;
}

export interface ReviewPagination {
  currentPage: number;
  totalPages: number;
  totalItems: number;
  links?: PaginationLinks;
}

export function useProductReviews(productId: number | null) {
  const items = ref<ReviewEntry[]>([]);
  const isSubmitting = ref(false);
  const isLoading = ref(false);
  const userReviewId = ref<number | null>(null);
  const currentUserId = ref<number | null>(null);
  const activeProductId = ref<number | null>(productId);
  const reviewsEtag = ref<string | null>(null);
  const pagination = reactive<ReviewPagination>({
    currentPage: 1,
    totalPages: 1,
    totalItems: 0,
  });

  const form = reactive({
    rating: undefined as number | undefined,
    description: '',
  });

  const { showError, showSuccess } = useNotifications();
  const reviewersStore = useReviewersStore();

  const hasUserReview = computed(() => userReviewId.value !== null);
  const canSubmit = computed(() => !isSubmitting.value && form.rating !== null && form.description.trim().length > 0);

  const resetForm = () => {
    form.rating = undefined;
    form.description = '';
  };

  const setCurrentUserId = (id: number | null) => {
    currentUserId.value = id;
    if (id === null) {
      userReviewId.value = null;
      resetForm();
      return;
    }

    const own = items.value.find((entry) => entry.reviewerId === id);
    if (own) {
      userReviewId.value = own.id;
      form.rating = own.rating;
      form.description = own.description || '';
    } else {
      userReviewId.value = null;
      resetForm();
    }
  };

  const parseIdFromUri = (uri?: string): number | null => {
    if (!uri) return null;
    const match = uri.match(/\/(\d+)(?:\/?$)/);
    return match ? Number(match[1]) : null;
  };

  const loadReviews = async (params?: Record<string, any>) => {
    if (!activeProductId.value) return;
    isLoading.value = true;
    try {
      const pageParams = {
        ...params,
        page: pagination.currentPage - 1,
      };
      const response = await reviewService.list(activeProductId.value, pageParams, reviewsEtag.value);

      if (response.notModified) {
        isLoading.value = false;
        return;
      }

      const data: Review[] = response.data || [];
      const enriched = await Promise.all(
        data.map(async (entry: Review) => {
          const reviewerUri: string | undefined = entry.reviewerUri;
          let reviewer: Reviewer | undefined;
          if (reviewerUri) {
            try {
              reviewer = await reviewersStore.fetch(reviewerUri);
            } catch (error) {
              console.error('Failed to fetch reviewer for review', entry.id, error);
            }
          }

          return {
            id: entry.id,
            rating: Number(entry.rating) || 0,
            description: entry.description,
            createdAt: entry.createdAt,
            reviewer,
            reviewerUri,
            reviewerId: parseIdFromUri(reviewerUri),
          } as ReviewEntry;
        }),
      );

      items.value = enriched;
      reviewsEtag.value = response.etag;

      const pageSize = 12;
      pagination.totalItems = response.totalCount;
      pagination.totalPages = Math.ceil(response.totalCount / pageSize) || 1;
      pagination.links = response.links;

      if (currentUserId.value !== null) {
        const own = enriched.find((entry) => entry.reviewerId === currentUserId.value);
        userReviewId.value = own ? own.id : null;
        if (own) {
          form.rating = own.rating;
          form.description = own.description || '';
        } else {
          resetForm();
        }
      }
    } catch (error) {
      showError('product_detail.review_feedback.load_failed');
      console.error('Failed to load reviews', error);
    } finally {
      isLoading.value = false;
    }
  };

  const handlePageChange = (page: number) => {
    pagination.currentPage = page;
    loadReviews();
  };

  const handleLinkNavigation = (url: string) => {
    const urlObj = new URL(url);
    const pageParam = urlObj.searchParams.get('page');
    if (pageParam !== null) {
      const page = parseInt(pageParam, 10);
      if (!isNaN(page)) {
        handlePageChange(page + 1);
      }
    }
  };

  const submitReview = async () => {
    if (!activeProductId.value || !canSubmit.value || form.rating === null) return;
    isSubmitting.value = true;
    const payload: CreateReviewPayload = {
      rating: form.rating,
      description: form.description.trim(),
    };

    try {
      if (userReviewId.value !== null) {
        await reviewService.update(activeProductId.value, userReviewId.value, payload);
        showSuccess('product_detail.review_feedback.updated');
      } else {
        await reviewService.create(activeProductId.value, payload);
        showSuccess('product_detail.review_feedback.created');
      }

      reviewsEtag.value = null;
      pagination.currentPage = 1;
      await loadReviews();
      if (!hasUserReview.value) {
        resetForm();
      }
    } catch (error: any) {
      showError('product_detail.review_feedback.failed');
      console.error('Review submission failed', error?.response || error);
    } finally {
      isSubmitting.value = false;
    }
  };

  const deleteReview = async () => {
    if (!activeProductId.value || userReviewId.value === null) return;
    isSubmitting.value = true;

    try {
      await reviewService.remove(activeProductId.value, userReviewId.value);
      showSuccess('product_detail.review_feedback.deleted');
      userReviewId.value = null;
      reviewsEtag.value = null;
      pagination.currentPage = 1;
      await loadReviews();
      resetForm();
    } catch (error) {
      showError('product_detail.review_feedback.failed');
      console.error('Failed to delete review', error);
    } finally {
      isSubmitting.value = false;
    }
  };

  const setProductId = (id: number | null) => {
    activeProductId.value = id;
    items.value = [];
    userReviewId.value = null;
    if (id === null) {
      resetForm();
    }
  };

  return {
    form,
    items,
    isLoading,
    isSubmitting,
    hasUserReview,
    canSubmit,
    pagination,
    submitReview,
    loadReviews,
    deleteReview,
    resetForm,
    userReviewId,
    setCurrentUserId,
    setProductId,
    handlePageChange,
    handleLinkNavigation,
  };
}
