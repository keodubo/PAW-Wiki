import { describe, it, expect, vi, beforeEach } from 'vitest';
import { nextTick } from 'vue';
import { useProductReviews } from '@/composables/useProductReviews';

const showError = vi.fn();
const showSuccess = vi.fn();

vi.mock('@/composables/useNotifications', () => ({
  useNotifications: () => ({
    showError,
    showSuccess,
  }),
}));

const reviewersFetch = vi.fn();

vi.mock('@/stores/reviewers', () => ({
  useReviewersStore: () => ({
    fetch: reviewersFetch,
  }),
}));

const reviewListMock = vi.fn();
const reviewCreateMock = vi.fn();
const reviewUpdateMock = vi.fn();
const reviewDeleteMock = vi.fn();

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    accessToken: null,
    refreshToken: null,
  }),
}));

vi.mock('@/services/ReviewService', () => ({
  reviewService: {
    list: (...args: any[]) => reviewListMock(...args),
    create: (...args: any[]) => reviewCreateMock(...args),
    update: (...args: any[]) => reviewUpdateMock(...args),
    remove: (...args: any[]) => reviewDeleteMock(...args),
  },
}));

const flush = async () => {
  await nextTick();
  await nextTick();
};

describe('useProductReviews', () => {
  beforeEach(() => {
    showError.mockClear();
    showSuccess.mockClear();
    reviewersFetch.mockReset();
    reviewListMock.mockReset();
    reviewCreateMock.mockReset();
    reviewUpdateMock.mockReset();
    reviewDeleteMock.mockReset();
    reviewListMock.mockResolvedValue({
      data: [
        { id: 1, rating: 4, description: 'good', reviewerUri: '/reviewers/1', createdAt: '2025-01-01', selfUri: '/reviews/1', productUri: '/products/10' },
        { id: 2, rating: 5, description: 'great', reviewerUri: '/reviewers/2', createdAt: '2025-01-02', selfUri: '/reviews/2', productUri: '/products/10' },
      ],
    });
    reviewersFetch.mockImplementation((uri: string) => {
      const id = parseInt(uri.split('/').pop() || '0', 10);
      return { id, name: `Reviewer ${id}` };
    });
  });

  it('loads reviews, enriches reviewers, and sets user review data', async () => {
    const reviews = useProductReviews(10);
    reviews.setCurrentUserId(1);

    await reviews.loadReviews();
    await flush();

    expect(reviewListMock).toHaveBeenCalledWith(10, { page: 0 }, null);
    expect(reviews.items.value).toHaveLength(2);
    expect(reviews.userReviewId.value).toBe(1);
    expect(reviews.form.rating).toBe(4);
    expect(reviews.form.description).toBe('good');
  });

  it('submits a new review and reloads', async () => {
    const reviews = useProductReviews(10);
    reviewListMock.mockResolvedValueOnce({ data: [] });
    await reviews.loadReviews();
    await flush();
    reviewCreateMock.mockResolvedValue(undefined);
    reviewListMock.mockResolvedValueOnce({ data: [] });

    reviews.form.rating = 5;
    reviews.form.description = 'great';
    await reviews.submitReview();

    expect(reviewCreateMock).toHaveBeenCalledWith(10, { rating: 5, description: 'great' });
    expect(showSuccess).toHaveBeenCalled();
    expect(reviewListMock).toHaveBeenCalledTimes(2);
  });

  it('updates existing review when userReviewId is present', async () => {
    const reviews = useProductReviews(10);
    reviews.userReviewId.value = 2;
    reviews.form.rating = 3;
    reviews.form.description = 'ok';
    reviewUpdateMock.mockResolvedValue(undefined);
    reviewListMock.mockResolvedValueOnce({ data: [] });

    await reviews.submitReview();

    expect(reviewUpdateMock).toHaveBeenCalledWith(10, 2, { rating: 3, description: 'ok' });
    expect(showSuccess).toHaveBeenCalled();
  });

  it('deletes review and resets form', async () => {
    const reviews = useProductReviews(10);
    reviews.userReviewId.value = 1;
    reviewDeleteMock.mockResolvedValue(undefined);
    reviewListMock.mockResolvedValueOnce({ data: [] });

    await reviews.deleteReview();

    expect(reviewDeleteMock).toHaveBeenCalledWith(10, 1);
    expect(showSuccess).toHaveBeenCalled();
    expect(reviews.form.description).toBe('');
    expect(reviews.userReviewId.value).toBeNull();
  });
});
