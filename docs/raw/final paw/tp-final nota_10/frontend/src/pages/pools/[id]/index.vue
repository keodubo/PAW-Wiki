<template>
  <v-container class="py-8 pool-detail-page" fluid>
    <div class="page-wrap">
      <v-row v-if="isPoolOwner" class="mb-4" justify="center">
        <v-col v-if="canEdit" cols="12" sm="4" mx="auto">
          <v-btn color="primary" block @click="goToEdit">
            <v-icon start>mdi-pencil</v-icon>
            {{ t('pool_detail.edit') }}
          </v-btn>
        </v-col>

        <v-col v-if="pool?.status === PoolStatus.AVAILABLE" cols="12" sm="4" mx="auto">
          <v-btn color="info" block @click="handlePause" :loading="actionLoading">
            <v-icon start>mdi-pause</v-icon>
            {{ t('pool_detail.pause') }}
          </v-btn>
        </v-col>

        <v-col v-if="pool?.status === PoolStatus.PAUSED" cols="12" sm="4" mx="auto">
          <v-btn color="info" block @click="handleResume" :loading="actionLoading">
            <v-icon start>mdi-play</v-icon>
            {{ t('pool_detail.resume') }}
          </v-btn>
        </v-col>

        <v-col v-if="canStartDelivery" cols="12" sm="4" mx="auto">
          <v-btn color="warning" block @click="handleStartDelivery" :loading="actionLoading">
            <v-icon start>mdi-truck</v-icon>
            {{ t('pool_detail.start_delivery') }}
          </v-btn>
        </v-col>

        <v-col v-if="canCancel" cols="12" sm="4" mx="auto">
          <v-btn color="error" block @click="showCancelDialog = true">
            <v-icon start>mdi-close-circle</v-icon>
            {{ t('pool_detail.cancel_pool') }}
          </v-btn>
        </v-col>

        <v-col v-if="canFinish" cols="12" sm="4" mx="auto">
          <v-btn color="success" block @click="handleFinish" :loading="actionLoading">
            <v-icon start>mdi-flag-checkered</v-icon>
            {{ t('pool_detail.finish') }}
          </v-btn>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="7">
          <v-card class="elevation-3 rounded-xl mb-4">
            <v-card-text class="pa-6">
              <div class="d-flex justify-space-between align-center mb-3">
                <h2 class="text-h4 font-weight-bold">
                  <router-link v-if="product" :to="`/products/${product.id}`" class="product-link">
                    <span class="text-primary">
                      {{ product.name }}
                    </span>
                  </router-link>
                  <span v-else>
                    {{ t('pool_detail.pool_fallback') }}
                  </span>
                </h2>
                <v-chip v-if="company" prepend-icon="mdi-domain" color="primary" variant="flat" size="large" class="company-chip elevation-2" @click="router.push(`/companies/${company.id}`)">
                  {{ companyName }}
                </v-chip>
              </div>

              <div class="text-h5 font-weight-bold text-success mb-4">
                {{ formatCurrency(pool?.price || 0) }}
              </div>

              <div class="d-flex flex-wrap ga-2 mb-4">
                <v-chip size="small" prepend-icon="mdi-tag" variant="tonal">
                  {{ categoryName }}
                </v-chip>
                <v-chip size="small" prepend-icon="mdi-map-marker" variant="tonal" color="secondary">
                  {{ locationName }}
                </v-chip>
              </div>

              <div class="text-body-2 text-medium-emphasis mb-2">
                {{ t('pool_detail.created_on', { date: createdDate || t('pool_detail.unknown_date') }) }}
              </div>

              <div v-if="pool?.downPayment && pool.downPayment > 0" class="text-body-2 text-medium-emphasis mb-2">{{ t('pool_detail.down_payment', { percent: pool.downPayment }) }}</div>
              <div v-else class="text-body-2 text-medium-emphasis mb-2">
                {{ t('pool_detail.no_down_payment') }}
              </div>

              <div class="text-body-1 my-4">{{ product?.description }}</div>

              <div class="mt-4">
                <v-progress-linear :model-value="progressPercent" height="16" :color="progressColor" bg-color="grey-lighten-3" class="mb-2" rounded>
                  <template #default>
                    <div class="text-caption font-weight-bold">{{ Math.round(progressPercent) }}%</div>
                  </template>
                </v-progress-linear>
                <p class="text-center text-caption text-medium-emphasis mb-0">{{ filledUnits }} / {{ totalUnits }} / {{ pool?.minQuantity || 0 }}</p>
              </div>
            </v-card-text>
          </v-card>

          <v-card v-if="!isPoolOwner && isClient" class="elevation-3 rounded-xl">
            <v-card-text class="pa-6">
              <template v-if="!userRequest && isPoolAvailable">
                <h3 class="text-h5 font-weight-bold text-primary mb-4">
                  {{ t('pool_detail.make_your_request') }}
                </h3>

                <v-alert v-if="!authStore.accountValidated" type="warning" variant="tonal" density="comfortable" class="mb-4" icon="mdi-alert">
                  <div class="text-body-2 font-weight-medium">
                    {{ t('pool_detail.validation_required') }}
                  </div>
                </v-alert>

                <v-alert v-if="pool?.downPayment && pool.downPayment > 0" type="warning" variant="tonal" density="comfortable" class="mb-4" icon="mdi-alert">
                  <div class="text-body-2">
                    <strong>{{ t('pool_detail.down_payment_warning_title') }}</strong
                    ><br />
                    {{ t('pool_detail.down_payment_warning_message', { percent: pool.downPayment }) }}
                  </div>
                </v-alert>

                <v-btn color="primary" size="large" block @click="openJoinDialog" :disabled="!authStore.accountValidated || !pool">
                  <v-icon start>mdi-hand-back-right</v-icon>
                  {{ t('pool_detail.make_your_request') }}
                </v-btn>
              </template>

              <template v-else-if="!userRequest && !isPoolAvailable">
                <v-alert :type="poolStatusAlertType" variant="tonal" density="comfortable" :icon="poolStatusIcon">
                  <div class="text-body-2 font-weight-medium">
                    {{ $t(poolStatusMessage) }}
                  </div>
                </v-alert>
              </template>

              <template v-if="userRequest">
                <div class="d-flex justify-space-between align-center mb-3">
                  <h3 class="text-h5 font-weight-bold text-primary">
                    {{ $t('pool_detail.you_requested', { count: userRequest.quantity }) }}
                  </h3>
                  <v-btn v-if="canDeleteRequest" color="error" icon="mdi-trash-can" size="small" variant="text" @click="showDeleteRequestDialog = true" />
                </div>

                <div class="text-h6 font-weight-bold mb-2">{{ $t('pool_detail.total_price') }}: {{ formatCurrency(userRequest.total) }}</div>

                <template v-if="pool?.downPayment && pool.downPayment > 0">
                  <div class="text-body-2 text-medium-emphasis">{{ $t('pool_detail.down_payment_amount') }}: {{ formatCurrency((userRequest.total * pool.downPayment) / 100) }}</div>
                  <div class="text-body-2 text-medium-emphasis mb-3">{{ $t('pool_detail.final_payment_amount') }}: {{ formatCurrency(userRequest.total * (1 - pool.downPayment / 100)) }}</div>
                </template>

                <div v-if="shouldShowCBU" class="text-body-2 font-weight-medium text-secondary mb-3">{{ t('pool_detail.company_cbu') }}: {{ company?.cbu }}</div>

                <div class="mb-3">
                  <span class="text-body-2 text-medium-emphasis">{{ t('pool_detail.status_label_text') }}: </span>
                  <v-chip :color="requestStatusColor" size="small" variant="flat" class="font-weight-bold">
                    {{ $t(requestStatusText) }}
                  </v-chip>
                </div>

                <v-row v-if="userRequest.status === RequestStatus.PENDING" class="mt-2 compact-buttons-row">
                  <v-col v-if="pool?.downPayment && pool.downPayment > 0 && !userRequest.downPaymentUri" cols="12" class="compact-button-col">
                    <v-btn color="warning" block @click="openUploadDownPaymentDialog">
                      <v-icon start>mdi-upload</v-icon>
                      {{ $t('pool_detail.upload_down_payment') }}
                    </v-btn>
                  </v-col>
                  <v-col v-if="pool?.downPayment && pool.downPayment > 0 && userRequest.downPaymentUri" cols="12" class="compact-button-col">
                    <v-btn color="info" block @click="viewReceipt(userRequest.downPaymentUri!)">
                      <v-icon start>mdi-eye</v-icon>
                      {{ $t('pool_detail.view_down_payment_receipt') }}
                    </v-btn>
                  </v-col>
                  <v-col v-if="pool?.downPayment && pool.downPayment > 0 && userRequest.downPaymentUri" cols="12" class="compact-button-col">
                    <v-btn color="warning" block @click="openUploadDownPaymentDialog">
                      <v-icon start>mdi-pencil</v-icon>
                      {{ $t('pool_detail.edit_down_payment_receipt') }}
                    </v-btn>
                  </v-col>

                  <v-col v-if="canEditRequest" cols="12" class="compact-button-col">
                    <v-btn color="primary" block @click="openEditRequestDialog">
                      <v-icon start>mdi-pencil</v-icon>
                      {{ t('pool_detail.edit_request') }}
                    </v-btn>
                  </v-col>
                </v-row>

                <v-row v-if="userRequest.status === RequestStatus.ACCEPTED && pool?.status === PoolStatus.DELIVERING" class="mt-2 compact-buttons-row">
                  <v-col v-if="!userRequest.finalPaymentUri" cols="12" class="compact-button-col">
                    <v-btn color="warning" block @click="openUploadFinalPaymentDialog">
                      <v-icon start>mdi-upload</v-icon>
                      {{ t('pool_detail.upload_final_payment') }}
                    </v-btn>
                  </v-col>
                  <v-col v-if="userRequest.finalPaymentUri" cols="12" class="compact-button-col">
                    <v-btn color="info" block @click="viewReceipt(userRequest.finalPaymentUri!)">
                      <v-icon start>mdi-eye</v-icon>
                      {{ t('pool_detail.view_final_payment_receipt') }}
                    </v-btn>
                  </v-col>
                  <v-col v-if="userRequest.finalPaymentUri" cols="12" class="compact-button-col">
                    <v-btn color="warning" block @click="openUploadFinalPaymentDialog">
                      <v-icon start>mdi-pencil</v-icon>
                      {{ t('pool_detail.edit_final_payment_receipt') }}
                    </v-btn>
                  </v-col>
                </v-row>
              </template>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="5">
          <v-card class="elevation-3 rounded-xl overflow-hidden">
            <v-img :src="productImage" :alt="product?.name" height="450" cover class="product-image">
              <template #placeholder>
                <div class="d-flex align-center justify-center fill-height">
                  <v-progress-circular indeterminate color="grey"></v-progress-circular>
                </div>
              </template>
            </v-img>
            <div :class="['status-badge', `status-${pool?.status?.toLowerCase()}`]">
              <div class="status-text">{{ $t(poolStatusBadge) }}</div>
            </div>
          </v-card>
        </v-col>
      </v-row>

      <v-card v-if="isPoolOwner" class="elevation-3 rounded-xl mt-6" id="requests">
        <div class="request-tabs-bar d-flex align-center bg-primary">
          <v-tabs v-model="requestTab" bg-color="primary" dark class="flex-grow-1">
            <v-tab v-if="showPendingTab" value="pending"> {{ $t('pool_detail.pending_requests') }} ({{ pendingRequestsCount || 0 }}) </v-tab>
            <v-tab v-if="showAcceptedTab" value="accepted"> {{ pool?.status === PoolStatus.DELIVERING ? $t('pool_detail.undelivered_requests') : $t('pool_detail.accepted_requests') }} ({{ acceptedRequestsCount || 0 }}) </v-tab>
            <v-tab v-if="showDeliveredTab" value="delivered"> {{ $t('pool_detail.delivered_requests') }} ({{ deliveredRequestsCount || 0 }}) </v-tab>
            <v-tab value="rejected"> {{ $t('pool_detail.rejected_requests') }} ({{ rejectedRequestsCount || 0 }}) </v-tab>
          </v-tabs>
          <v-btn icon variant="plain" class="refresh-requests-btn" :loading="requestsLoading" :title="t('refresh_requests')" @click="loadRequests">
            <span class="refresh-btn-circle">
              <v-icon color="primary">mdi-refresh</v-icon>
            </span>
          </v-btn>
        </div>

        <v-card-text class="pa-0">
          <v-window v-model="requestTab">
            <v-window-item v-if="showPendingTab" value="pending">
              <request-list :requests="pendingRequests" :loading="requestsLoading" status="pending" :pool-status="pool?.status" :pool-down-payment="pool?.downPayment || 0" :pool-price="pool?.price || 0" @reload="loadPoolAndRequests" />
            </v-window-item>
            <v-window-item v-if="showAcceptedTab" value="accepted">
              <request-list
                :requests="acceptedRequests"
                :loading="requestsLoading"
                :status="pool?.status === PoolStatus.DELIVERING ? 'undelivered' : 'accepted'"
                :pool-status="pool?.status"
                :pool-down-payment="pool?.downPayment || 0"
                :pool-price="pool?.price || 0"
                @reload="loadPoolAndRequests"
              />
            </v-window-item>
            <v-window-item v-if="showDeliveredTab" value="delivered">
              <request-list :requests="deliveredRequests" :loading="requestsLoading" status="delivered" :pool-status="pool?.status" :pool-down-payment="pool?.downPayment || 0" :pool-price="pool?.price || 0" @reload="loadPoolAndRequests" />
            </v-window-item>
            <v-window-item value="rejected">
              <request-list :requests="rejectedRequests" :loading="requestsLoading" status="rejected" :pool-status="pool?.status" :pool-down-payment="pool?.downPayment || 0" :pool-price="pool?.price || 0" @reload="loadPoolAndRequests" />
            </v-window-item>
          </v-window>
        </v-card-text>
      </v-card>
    </div>

    <v-dialog v-model="showJoinDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-hand-back-right</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ t('pool_detail.join_dialog_title') }}
            </h2>
            <p class="text-body-2 text-white opacity-90">
              {{ t('pool_detail.join_dialog_description') }}
            </p>
          </div>
        </v-card-title>

        <v-card-text class="pa-6">
          <v-form ref="joinForm" @submit.prevent="handleJoinPool">
            <v-text-field
              v-model.number="joinQuantity"
              :label="t('pool_detail.quantity_label')"
              type="number"
              :rules="quantityRules"
              variant="outlined"
              density="comfortable"
              prepend-inner-icon="mdi-counter"
              color="primary"
              class="mb-4"
              min="1"
              required
            />

            <v-card class="price-summary-card mb-4" variant="outlined">
              <v-card-text class="pa-4">
                <div class="d-flex justify-space-between align-center mb-3">
                  <span class="text-body-1 font-weight-medium">{{ t('pool_detail.unit_price') }}:</span>
                  <span class="text-body-1">{{ formatCurrency(pool?.price || 0) }}</span>
                </div>
                <div class="d-flex justify-space-between align-center mb-3">
                  <span class="text-body-1 font-weight-medium">{{ t('pool_detail.quantity_label') }}:</span>
                  <span class="text-body-1">{{ joinQuantity || 0 }}</span>
                </div>
                <v-divider class="my-3" />
                <div class="d-flex justify-space-between align-center mb-2">
                  <span class="text-h6 font-weight-bold text-primary">{{ t('pool_detail.total_price') }}:</span>
                  <span class="text-h6 font-weight-bold text-primary">{{ formatCurrency(totalPrice) }}</span>
                </div>
                <template v-if="pool?.downPayment && pool.downPayment > 0">
                  <v-divider class="my-3" />
                  <div class="d-flex justify-space-between align-center mb-2">
                    <span class="text-body-2 text-medium-emphasis">{{ t('pool_detail.down_payment_amount') }} ({{ pool.downPayment }}%):</span>
                    <span class="text-body-2 font-weight-medium text-warning">{{ formatCurrency(downPaymentAmount) }}</span>
                  </div>
                  <div class="d-flex justify-space-between align-center">
                    <span class="text-body-2 text-medium-emphasis">{{ t('pool_detail.final_payment_amount') }}:</span>
                    <span class="text-body-2 font-weight-medium">{{ formatCurrency(finalPaymentAmount) }}</span>
                  </div>
                </template>
              </v-card-text>
            </v-card>

            <v-alert type="info" variant="tonal" density="compact" class="mb-4" icon="mdi-information">
              <div class="text-body-2">
                <div class="mb-1">
                  <strong>{{ t('pool_detail.pool_progress') }}:</strong> {{ filledUnits }} / {{ pool?.minQuantity || 0 }} {{ t('pool_detail.units') }}
                </div>
                <div :class="remainingUnitsNeeded > 0 ? 'text-warning font-weight-medium' : 'text-success font-weight-medium'">
                  <v-icon size="small" :color="remainingUnitsNeeded > 0 ? 'warning' : 'success'" class="mr-1">
                    {{ remainingUnitsNeeded > 0 ? 'mdi-alert-circle' : 'mdi-check-circle' }}
                  </v-icon>
                  {{ remainingUnitsNeeded > 0 ? t('pool_detail.units_needed', { count: remainingUnitsNeeded }) : t('pool_detail.minimum_reached') }}
                </div>
              </div>
            </v-alert>

            <v-btn type="submit" color="primary" size="large" block :loading="isSubmitting" class="join-btn mb-3" elevation="2">
              <template #prepend v-if="!isSubmitting">
                <v-icon>mdi-check-circle</v-icon>
              </template>
              {{ t('pool_detail.confirm_join') }}
            </v-btn>

            <v-btn variant="outlined" color="grey" size="large" block @click="closeJoinDialog" :disabled="isSubmitting">
              <template #prepend>
                <v-icon>mdi-close</v-icon>
              </template>
              {{ t('pool_detail.cancel') }}
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showEditRequestDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-pencil</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ t('edit_request') }}
            </h2>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-form ref="editRequestForm" @submit.prevent="handleEditRequest">
            <v-text-field
              v-model="editRequestQuantity"
              :label="t('units')"
              type="number"
              min="1"
              variant="outlined"
              density="comfortable"
              prepend-inner-icon="mdi-package-variant"
              color="primary"
              class="mb-4"
              :rules="quantityRules"
              required
            />

            <v-btn type="submit" color="primary" size="large" block :loading="isSubmitting" class="join-btn mb-3" elevation="2">
              <template #prepend v-if="!isSubmitting">
                <v-icon>mdi-check-circle</v-icon>
              </template>
              {{ t('update') }}
            </v-btn>

            <v-btn variant="outlined" color="grey" size="large" block @click="showEditRequestDialog = false" :disabled="isSubmitting">
              <template #prepend>
                <v-icon>mdi-close</v-icon>
              </template>
              {{ t('cancel') }}
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showCancelDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-close-circle</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ t('pool_detail.cancel_pool') }}
            </h2>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-alert type="warning" variant="tonal" density="comfortable" icon="mdi-alert" class="mb-4">
            {{ t('pool_detail.cancel_pool_confirmation') }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4 d-flex justify-end ga-2">
          <v-btn variant="outlined" color="grey" size="large" @click="showCancelDialog = false" :disabled="actionLoading">
            <template #prepend>
              <v-icon>mdi-arrow-left</v-icon>
            </template>
            {{ t('pool_detail.back') }}
          </v-btn>
          <v-btn color="error" variant="flat" size="large" @click="handleCancel" :loading="actionLoading">
            <template #prepend v-if="!actionLoading">
              <v-icon>mdi-trash-can</v-icon>
            </template>
            {{ t('pool_detail.cancel') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showUploadDownPaymentDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-upload</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ t('pool_detail.upload_down_payment') }}
            </h2>
            <p class="text-body-2 text-white opacity-90">
              {{ t('pool_detail.upload_down_payment_description') }}
            </p>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-form ref="uploadDownPaymentForm" @submit.prevent="handleUploadDownPayment">
            <v-file-input
              v-model="downPaymentFile"
              :label="t('pool_detail.select_receipt_file')"
              accept=".pdf,.jpg,.jpeg,.png,image/*,application/pdf"
              variant="outlined"
              density="comfortable"
              prepend-inner-icon="mdi-file-document"
              color="primary"
              class="mb-4"
              :rules="fileRules"
              :error-messages="uploadError"
              show-size
              required
            />
            <v-alert type="info" variant="tonal" density="compact" class="mb-4" icon="mdi-information">
              {{ t('pool_detail.file_requirements') }}
            </v-alert>
            <v-btn type="submit" color="warning" size="large" block :loading="isUploading" class="dialog-action-btn mb-3">
              <template #prepend v-if="!isUploading">
                <v-icon>mdi-upload</v-icon>
              </template>
              {{ t('pool_detail.upload') }}
            </v-btn>
            <v-btn variant="outlined" color="grey" size="large" block @click="closeUploadDownPaymentDialog" :disabled="isUploading">
              <template #prepend>
                <v-icon>mdi-close</v-icon>
              </template>
              {{ t('pool_detail.cancel') }}
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showUploadFinalPaymentDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-upload</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ t('pool_detail.upload_final_payment') }}
            </h2>
            <p class="text-body-2 text-white opacity-90">
              {{ t('pool_detail.upload_final_payment_description') }}
            </p>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-form ref="uploadFinalPaymentForm" @submit.prevent="handleUploadFinalPayment">
            <v-file-input
              v-model="finalPaymentFile"
              :label="t('pool_detail.select_receipt_file')"
              accept=".pdf,.jpg,.jpeg,.png,image/*,application/pdf"
              variant="outlined"
              density="comfortable"
              prepend-inner-icon="mdi-file-document"
              color="primary"
              class="mb-4"
              :rules="fileRules"
              :error-messages="$t(uploadError)"
              show-size
              required
            />
            <v-alert type="info" variant="tonal" density="compact" class="mb-4" icon="mdi-information">
              {{ t('pool_detail.file_requirements') }}
            </v-alert>
            <v-btn type="submit" color="warning" size="large" block :loading="isUploading" class="dialog-action-btn mb-3">
              <template #prepend v-if="!isUploading">
                <v-icon>mdi-upload</v-icon>
              </template>
              {{ t('pool_detail.upload') }}
            </v-btn>
            <v-btn variant="outlined" color="grey" size="large" block @click="closeUploadFinalPaymentDialog" :disabled="isUploading">
              <template #prepend>
                <v-icon>mdi-close</v-icon>
              </template>
              {{ t('pool_detail.cancel') }}
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showDeleteRequestDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-trash-can</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ t('pool_detail.delete_request') }}
            </h2>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-alert type="warning" variant="tonal" density="comfortable" icon="mdi-alert" class="mb-4">
            {{ t('pool_detail.delete_request_confirmation') }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4 d-flex justify-end ga-2">
          <v-btn variant="outlined" color="grey" size="large" @click="showDeleteRequestDialog = false" :disabled="actionLoading">
            <template #prepend>
              <v-icon>mdi-close</v-icon>
            </template>
            {{ t('pool_detail.cancel') }}
          </v-btn>
          <v-btn color="error" variant="flat" size="large" @click="handleDeleteRequest" :loading="actionLoading">
            <template #prepend v-if="!actionLoading">
              <v-icon>mdi-trash-can</v-icon>
            </template>
            {{ t('pool_detail.delete') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import type { Pool } from '@/models/Pool';
import { PoolStatus } from '@/models/Pool';
import type { Product } from '@/models/Product';
import type { Company, Category, Location, Request } from '@/models';
import { RequestStatus } from '@/models/Request';
import { poolService, requestService, documentService } from '@/services';
import { useProductsStore } from '@/stores/products';
import { useCompaniesStore } from '@/stores/companies';
import { useCategoriesStore } from '@/stores/categories';
import { useLocationsStore } from '@/stores/locations';
import { useAuthStore } from '@/stores/auth';
import { useNotifications } from '@/composables/useNotifications';
import { formatCurrency } from '@/utils/currency';

const route = useRoute();
const router = useRouter();
const productsStore = useProductsStore();
const companiesStore = useCompaniesStore();
const categoriesStore = useCategoriesStore();
const locationsStore = useLocationsStore();
const authStore = useAuthStore();
const { t } = useI18n();
const { showSuccess, showError, handleApiError } = useNotifications();

const pool = ref<Pool | null>(null);
const product = ref<Product | null>(null);
const company = ref<Company | null>(null);
const category = ref<Category | null>(null);
const location = ref<Location | null>(null);
const isLoading = ref(true);
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

const showJoinDialog = ref(false);
const joinQuantity = ref<number>(1);
const isSubmitting = ref(false);
const joinForm = ref<any>(null);

const showEditRequestDialog = ref(false);
const editRequestQuantity = ref<number>(0);
const editRequestForm = ref<any>(null);

const actionLoading = ref(false);
const showCancelDialog = ref(false);
const showDeleteRequestDialog = ref(false);

const showUploadDownPaymentDialog = ref(false);
const showUploadFinalPaymentDialog = ref(false);
const downPaymentFile = ref<File | File[] | null>(null);
const finalPaymentFile = ref<File | File[] | null>(null);
const isUploading = ref(false);
const uploadError = ref('');
const uploadDownPaymentForm = ref<any>(null);
const uploadFinalPaymentForm = ref<any>(null);

const requestTab = ref('pending');
const pendingRequests = ref<Request[]>([]);
const acceptedRequests = ref<Request[]>([]);
const deliveredRequests = ref<Request[]>([]);
const rejectedRequests = ref<Request[]>([]);
const requestsLoading = ref(false);
const userRequest = ref<Request | null>(null);

const pendingRequestsCount = ref<number>(0);
const acceptedRequestsCount = ref<number>(0);
const deliveredRequestsCount = ref<number>(0);
const rejectedRequestsCount = ref<number>(0);

const productImage = computed(() => {
  if (product.value?.imageUri) {
    return product.value.imageUri;
  }
  return '';
});

const companyName = computed(() => company.value?.name || t('pool_detail.unknown_company'));
const categoryName = computed(() => category.value?.name || t('pool_detail.uncategorized'));
const locationName = computed(() => location.value?.name || t('pool_detail.no_location'));

const createdDate = computed(() => {
  if (!pool.value?.createdAt) return '';
  const d = new Date(pool.value.createdAt);
  return isNaN(d.getTime()) ? '' : d.toLocaleDateString();
});

const filledUnits = computed(() => {
  if (!pool.value?.requestsStats) return 0;
  return (pool.value.requestsStats.acceptedSum || 0) + (pool.value.requestsStats.deliveredSum || 0);
});

const pendingUnits = computed(() => {
  if (!pool.value?.requestsStats) return 0;
  return pool.value.requestsStats.pendingSum || 0;
});

const totalUnits = computed(() => filledUnits.value + pendingUnits.value);

const progressPercent = computed(() => {
  if (!pool.value) return 0;
  const total = Math.max(totalUnits.value, pool.value.minQuantity || 1);
  return Math.min(100, (filledUnits.value * 100) / total);
});

const progressColor = computed(() => {
  const percent = progressPercent.value;
  if (percent < 15) return 'error';
  if (percent < 50) return 'warning';
  return 'success';
});

const isPoolAvailable = computed(() => pool.value?.status === PoolStatus.AVAILABLE);

const isPoolOwner = computed(() => {
  return authStore.isCompany && company.value?.id === authStore.currentCompany?.id;
});

const isClient = computed(() => authStore.isUser && authStore.isAuthenticated);

const canEdit = computed(() => {
  return pool.value?.status === PoolStatus.AVAILABLE || pool.value?.status === PoolStatus.PAUSED;
});

const canStartDelivery = computed(() => {
  return (pool.value?.status === PoolStatus.AVAILABLE || pool.value?.status === PoolStatus.PAUSED) && filledUnits.value >= (pool.value?.minQuantity || 0);
});

const canCancel = computed(() => {
  return (pool.value?.status === PoolStatus.AVAILABLE || pool.value?.status === PoolStatus.PAUSED) && (pool.value?.requestsStats?.acceptedCount || 0) === 0 && (pool.value?.requestsStats?.deliveredCount || 0) === 0;
});

const canFinish = computed(() => {
  return pool.value?.status === PoolStatus.DELIVERING && (pool.value?.requestsStats?.acceptedCount || 0) === 0;
});

const poolStatusBadge = computed(() => {
  if (!pool.value) return '';
  switch (pool.value.status) {
    case PoolStatus.AVAILABLE:
      return 'pool_detail.status_available';
    case PoolStatus.DELIVERING:
      return 'pool_detail.status_delivering_badge';
    case PoolStatus.PAUSED:
      return 'pool_detail.status_paused_badge';
    case PoolStatus.CANCELLED:
      return 'pool_detail.status_cancelled_badge';
    case PoolStatus.FINISHED:
      return 'pool_detail.status_finished_badge';
    default:
      return '';
  }
});

const poolStatusAlertType = computed(() => {
  if (!pool.value) return 'info';
  switch (pool.value.status) {
    case PoolStatus.FINISHED:
      return 'success';
    case PoolStatus.CANCELLED:
      return 'error';
    case PoolStatus.PAUSED:
      return 'warning';
    case PoolStatus.DELIVERING:
      return 'info';
    default:
      return 'info';
  }
});

const poolStatusIcon = computed(() => {
  if (!pool.value) return 'mdi-information';
  switch (pool.value.status) {
    case PoolStatus.FINISHED:
      return 'mdi-check-circle';
    case PoolStatus.CANCELLED:
      return 'mdi-close-circle';
    case PoolStatus.PAUSED:
      return 'mdi-pause-circle';
    case PoolStatus.DELIVERING:
      return 'mdi-truck-delivery';
    default:
      return 'mdi-information';
  }
});

const poolStatusMessage = computed(() => {
  if (!pool.value) return '';
  switch (pool.value.status) {
    case PoolStatus.FINISHED:
      return 'pool_detail.status_finished';
    case PoolStatus.CANCELLED:
      return 'pool_detail.status_cancelled';
    case PoolStatus.PAUSED:
      return 'pool_detail.status_paused';
    case PoolStatus.DELIVERING:
      return 'pool_detail.status_delivering';
    default:
      return 'pool_detail.status_unavailable';
  }
});

const showPendingTab = computed(() => pool.value?.status === PoolStatus.AVAILABLE || pool.value?.status === PoolStatus.PAUSED);

const showAcceptedTab = computed(() => pool.value?.status === PoolStatus.AVAILABLE || pool.value?.status === PoolStatus.PAUSED || pool.value?.status === PoolStatus.DELIVERING);

const showDeliveredTab = computed(() => pool.value?.status === PoolStatus.DELIVERING || pool.value?.status === PoolStatus.FINISHED);

const requestStatusColor = computed(() => {
  if (!userRequest.value) return 'grey';
  switch (userRequest.value.status) {
    case RequestStatus.PENDING:
      return 'secondary';
    case RequestStatus.ACCEPTED:
      return 'info';
    case RequestStatus.DELIVERED:
      return 'success';
    case RequestStatus.REJECTED:
      return 'error';
    default:
      return 'grey';
  }
});

const requestStatusText = computed(() => {
  if (!userRequest.value) return '';
  const status = userRequest.value.status;

  if (status === RequestStatus.PENDING) {
    if (pool.value?.downPayment && !userRequest.value.downPaymentUri) {
      return 'pool_detail.status_pending_down_payment';
    }
    return 'pool_detail.status_pending';
  }

  if (status === RequestStatus.ACCEPTED) {
    if (pool.value?.status === PoolStatus.DELIVERING) {
      if (!userRequest.value.finalPaymentUri) {
        return 'pool_detail.status_pending_final_payment';
      }
      return 'pool_detail.status_not_delivered';
    }
    return 'pool_detail.status_accepted';
  }

  if (status === RequestStatus.DELIVERED) {
    return 'pool_detail.status_delivered';
  }

  if (status === RequestStatus.REJECTED) {
    return 'pool_detail.status_rejected';
  }

  return '';
});

const shouldShowCBU = computed(() => {
  if (!userRequest.value || !pool.value) return false;
  const needsDown = userRequest.value.status === RequestStatus.PENDING && pool.value.downPayment > 0 && !userRequest.value.downPaymentUri;
  const needsFinal = userRequest.value.status === RequestStatus.ACCEPTED && pool.value.status === PoolStatus.DELIVERING && !userRequest.value.finalPaymentUri;
  return needsDown || needsFinal;
});

const needsDownPaymentUpload = computed(() => {
  return pool.value?.downPayment && pool.value.downPayment > 0 && userRequest.value?.downPaymentUri == null;
});

const needsFinalPaymentUpload = computed(() => {
  return userRequest.value?.status === RequestStatus.ACCEPTED && pool.value?.status === PoolStatus.DELIVERING && userRequest.value?.finalPaymentUri == null;
});

const canEditRequest = computed(() => {
  return userRequest.value?.status === RequestStatus.PENDING && !userRequest.value?.downPaymentUri;
});

const canDeleteRequest = computed(() => {
  return (pool.value?.status === PoolStatus.AVAILABLE || pool.value?.status === PoolStatus.PAUSED) && userRequest.value?.status === RequestStatus.PENDING;
});

const quantityRules = computed(() => [(v: any) => !!v || t('pool_detail.quantity_required'), (v: any) => (v && v >= 1) || t('pool_detail.quantity_min')]);

const totalPrice = computed(() => {
  const quantity = joinQuantity.value || 0;
  const price = pool.value?.price || 0;
  return quantity * price;
});

const downPaymentAmount = computed(() => {
  const downPaymentPercent = pool.value?.downPayment || 0;
  return (totalPrice.value * downPaymentPercent) / 100;
});

const finalPaymentAmount = computed(() => {
  return totalPrice.value - downPaymentAmount.value;
});

const remainingUnitsNeeded = computed(() => {
  if (!pool.value) return 0;
  const minQuantity = pool.value.minQuantity || 0;
  const currentFilled = filledUnits.value;
  const remaining = minQuantity - currentFilled;
  return Math.max(0, remaining);
});

const loadPool = async () => {
  const id = Number((route.params as { id?: string }).id);
  if (Number.isNaN(id)) {
    router.replace('/pools');
    return;
  }
  try {
    isLoading.value = true;
    const fetched = await poolService.getById(id);
    pool.value = fetched;

    pendingRequestsCount.value = fetched.requestsStats?.pendingCount || 0;
    acceptedRequestsCount.value = fetched.requestsStats?.acceptedCount || 0;
    deliveredRequestsCount.value = fetched.requestsStats?.deliveredCount || 0;
    rejectedRequestsCount.value = fetched.requestsStats?.rejectedCount || 0;

    const fetchedProduct = fetched.productUri ? await productsStore.fetch(fetched.productUri) : null;
    product.value = fetchedProduct;

    if (fetchedProduct?.companyUri) company.value = await companiesStore.fetch(fetchedProduct.companyUri);
    if (fetchedProduct?.categoryUri) category.value = await categoriesStore.fetch(fetchedProduct.categoryUri);
    if (fetched.locationUri) location.value = await locationsStore.fetch(fetched.locationUri);
  } finally {
    isLoading.value = false;
  }
};

const loadRequests = async () => {
  if (!pool.value?.requestsUri || !isPoolOwner.value) return;

  try {
    requestsLoading.value = true;

    const [pending, accepted, delivered, rejected] = await Promise.all([
      requestService.list({ pool_id: pool.value.id, status: RequestStatus.PENDING }),
      requestService.list({ pool_id: pool.value.id, status: RequestStatus.ACCEPTED }),
      requestService.list({ pool_id: pool.value.id, status: RequestStatus.DELIVERED }),
      requestService.list({ pool_id: pool.value.id, status: RequestStatus.REJECTED }),
    ]);

    pendingRequests.value = pending.data;
    acceptedRequests.value = accepted.data;
    deliveredRequests.value = delivered.data;
    rejectedRequests.value = rejected.data;
    pendingRequestsCount.value = pending.data.length;
    acceptedRequestsCount.value = accepted.data.length;
    deliveredRequestsCount.value = delivered.data.length;
    rejectedRequestsCount.value = rejected.data.length;
  } catch (error) {
    console.error('Failed to load requests:', error);
  } finally {
    requestsLoading.value = false;
  }
};

const loadUserRequest = async () => {
  if (!authStore.isUser || !authStore.accountValidated || !pool.value) return;

  try {
    const response = await requestService.list({
      pool_id: pool.value.id,
      user_id: authStore.userId || undefined,
    });

    if (response.data && response.data.length > 0) {
      userRequest.value = response.data[0];
    }
  } catch (error) {
    console.error('Failed to load user request:', error);
  }
};

const loadPoolAndRequests = async () => {
  await loadPool();
  if (isPoolOwner.value) {
    await loadRequests();
  } else if (isClient.value) {
    await loadUserRequest();
  }
};

const handlePause = async () => {
  if (!pool.value) return;
  try {
    actionLoading.value = true;
    await poolService.pause(pool.value.id);
    showSuccess('pool_detail.pool_paused');
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = false;
  }
};

const handleResume = async () => {
  if (!pool.value) return;
  try {
    actionLoading.value = true;
    await poolService.await(pool.value.id);
    showSuccess('pool_detail.pool_resumed');
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = false;
  }
};

const handleStartDelivery = async () => {
  if (!pool.value) return;
  try {
    actionLoading.value = true;
    await poolService.deliver(pool.value.id);
    showSuccess('pool_detail.delivery_started');
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = false;
  }
};

const handleCancel = async () => {
  if (!pool.value) return;
  try {
    actionLoading.value = true;
    await poolService.cancel(pool.value.id);
    showSuccess('pool_detail.pool_cancelled');
    showCancelDialog.value = false;
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = false;
  }
};

const handleFinish = async () => {
  if (!pool.value) return;
  try {
    actionLoading.value = true;
    await poolService.finish(pool.value.id);
    showSuccess('pool_detail.pool_finished');
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = false;
  }
};

const handleDeleteRequest = async () => {
  if (!userRequest.value) return;
  try {
    actionLoading.value = true;
    await requestService.delete(userRequest.value.id);
    showSuccess('pool_detail.request_deleted');
    showDeleteRequestDialog.value = false;
    userRequest.value = null;
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = false;
  }
};

const goBack = () => {
  router.push('/pools');
};

const goToEdit = () => {
  if (pool.value?.id) {
    router.push(`/pools/${pool.value.id}/edit`);
  }
};

const openEditRequestDialog = () => {
  if (userRequest.value) {
    editRequestQuantity.value = userRequest.value.quantity;
    showEditRequestDialog.value = true;
  }
};

const handleEditRequest = async () => {
  if (!editRequestForm.value) return;
  const { valid } = await editRequestForm.value.validate();
  if (!valid) return;

  if (!userRequest.value?.id) return;

  try {
    isSubmitting.value = true;
    await requestService.edit(userRequest.value.id, { quantity: Number(editRequestQuantity.value) });
    showSuccess('pool_detail.request_updated');
    showEditRequestDialog.value = false;
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
  } finally {
    isSubmitting.value = false;
  }
};

const viewReceipt = async (documentUri: string) => {
  try {
    const blob = await documentService.getByUri(documentUri);
    const url = URL.createObjectURL(blob);
    window.open(url, '_blank');

    setTimeout(() => URL.revokeObjectURL(url), 1000);
  } catch (error) {
    handleApiError(error);
  }
};

const openUploadDownPaymentDialog = () => {
  downPaymentFile.value = null;
  uploadError.value = '';
  showUploadDownPaymentDialog.value = true;
};

const closeUploadDownPaymentDialog = () => {
  showUploadDownPaymentDialog.value = false;
  downPaymentFile.value = null;
  uploadError.value = '';
};

const openUploadFinalPaymentDialog = () => {
  finalPaymentFile.value = null;
  uploadError.value = '';
  showUploadFinalPaymentDialog.value = true;
};

const closeUploadFinalPaymentDialog = () => {
  showUploadFinalPaymentDialog.value = false;
  finalPaymentFile.value = null;
  uploadError.value = '';
};

const handleUploadDownPayment = async () => {
  if (!uploadDownPaymentForm.value) return;

  const { valid } = await uploadDownPaymentForm.value.validate();
  if (!valid) return;

  if (!userRequest.value?.id || !downPaymentFile.value) {
    uploadError.value = t('pool_detail.file_required');
    return;
  }

  try {
    isUploading.value = true;
    uploadError.value = '';

    const file = Array.isArray(downPaymentFile.value) ? downPaymentFile.value[0] : downPaymentFile.value;

    if (!file || !(file instanceof File)) {
      uploadError.value = t('pool_detail.file_required');
      return;
    }

    const documentUri = await documentService.uploadReceipt(file);

    await requestService.uploadDownPayment(userRequest.value.id, documentUri);

    showSuccess('pool_detail.down_payment_uploaded');
    closeUploadDownPaymentDialog();
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
    uploadError.value = error instanceof Error ? error.message : 'pool_detail.upload_failed';
  } finally {
    isUploading.value = false;
  }
};

const handleUploadFinalPayment = async () => {
  if (!uploadFinalPaymentForm.value) return;

  const { valid } = await uploadFinalPaymentForm.value.validate();
  if (!valid) return;

  if (!userRequest.value?.id || !finalPaymentFile.value) {
    uploadError.value = t('pool_detail.file_required');
    return;
  }

  try {
    isUploading.value = true;
    uploadError.value = '';

    const file = Array.isArray(finalPaymentFile.value) ? finalPaymentFile.value[0] : finalPaymentFile.value;

    if (!file || !(file instanceof File)) {
      uploadError.value = t('pool_detail.file_required');
      return;
    }

    const documentUri = await documentService.uploadReceipt(file);

    await requestService.uploadFinalPayment(userRequest.value.id, documentUri);

    showSuccess('pool_detail.final_payment_uploaded');
    closeUploadFinalPaymentDialog();
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
    uploadError.value = error instanceof Error ? error.message : 'pool_detail.upload_failed';
  } finally {
    isUploading.value = false;
  }
};

const openJoinDialog = () => {
  if (!isPoolAvailable.value) {
    showError('pool_detail.pool_not_available');
    return;
  }
  joinQuantity.value = 1;
  showJoinDialog.value = true;
};

const closeJoinDialog = () => {
  showJoinDialog.value = false;
  joinQuantity.value = 1;
};

const handleJoinPool = async () => {
  if (!joinForm.value) return;

  const { valid } = await joinForm.value.validate();
  if (!valid) return;

  if (!pool.value?.id) return;

  if (!isPoolAvailable.value) {
    showError('pool_detail.pool_not_available');
    closeJoinDialog();
    return;
  }

  try {
    isSubmitting.value = true;
    await requestService.create(pool.value.selfUri, joinQuantity.value);
    showSuccess('pool_detail.join_success');
    closeJoinDialog();
    await loadPoolAndRequests();
  } catch (error) {
    handleApiError(error);
  } finally {
    isSubmitting.value = false;
  }
};

const fileRules = [
  (v: File | File[] | null) => {
    if (!v) return t('pool_detail.file_required');

    const file = Array.isArray(v) ? v[0] : v;
    if (!file || !(file instanceof File)) return t('pool_detail.file_required');
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    if (!allowedTypes.includes(file.type)) {
      return t('pool_detail.invalid_file_type');
    }
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      return t('pool_detail.file_too_large');
    }
    return true;
  },
];

onMounted(() => {
  loadPoolAndRequests();
});
</script>

<style scoped>
.pool-detail-page {
  background: rgb(var(--v-theme-background));
  min-height: 100vh;
}

.page-wrap {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.product-image {
  background-color: rgba(var(--v-theme-primary), 0.03);
}

.status-badge {
  padding: 16px;
  text-align: center;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-available {
  background-color: #4caf50;
  color: white;
}

.status-delivering {
  background-color: #2196f3;
  color: white;
}

.status-paused {
  background-color: #ff9800;
  color: white;
}

.status-cancelled {
  background-color: #f44336;
  color: white;
}

.status-finished {
  background-color: #9c27b0;
  color: white;
}

.join-dialog-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.join-dialog-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.join-dialog-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%), radial-gradient(circle at 70% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.header-section {
  position: relative;
  z-index: 2;
}

.join-btn {
  height: 48px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.join-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--v-theme-primary), 0.4) !important;
}

.join-dialog-card :deep(.v-text-field .v-field) {
  border-radius: 12px;
  background: #f8f9fa;
}

.join-dialog-card :deep(.v-text-field .v-field:hover) {
  background: #f1f3f4;
}

.join-dialog-card :deep(.v-text-field .v-field--focused .v-field__outline) {
  border-color: rgb(var(--v-theme-primary)) !important;
}

.join-dialog-card :deep(.v-text-field .v-field--focused .v-label) {
  color: rgb(var(--v-theme-primary)) !important;
}

.join-dialog-card :deep(.v-text-field .v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary));
}

.price-summary-card {
  border: 2px solid rgba(var(--v-theme-primary), 0.2) !important;
  background: linear-gradient(135deg, rgba(var(--v-theme-primary), 0.03), rgba(var(--v-theme-secondary), 0.03));
  border-radius: 12px !important;
  transition: all 0.3s ease;
}

.price-summary-card:hover {
  border-color: rgba(var(--v-theme-primary), 0.4) !important;
  box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.1);
}

.v-theme--dark .join-dialog-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
}

.v-theme--dark .join-dialog-card :deep(.v-text-field .v-field) {
  background: #2a2a2a !important;
  color: white !important;
}

.v-theme--dark .join-dialog-card :deep(.v-text-field .v-field:hover) {
  background: #333333 !important;
}

.v-theme--dark .price-summary-card {
  background: linear-gradient(135deg, rgba(var(--v-theme-primary), 0.08), rgba(var(--v-theme-secondary), 0.08)) !important;
  border-color: rgba(var(--v-theme-primary), 0.3) !important;
}

.dialog-action-btn {
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.dialog-action-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--v-theme-error), 0.3) !important;
}

.join-dialog-card .v-card-text .v-btn[type='submit'],
.join-dialog-card .v-card-text .v-btn.block {
  width: 100% !important;
}

.company-chip {
  cursor: pointer !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600 !important;
  letter-spacing: 0.5px;
  padding: 20px 16px !important;
  border-radius: 12px !important;
}

.company-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(var(--v-theme-primary), 0.4) !important;
}

.company-chip:active {
  transform: translateY(0px);
  box-shadow: 0 2px 8px rgba(var(--v-theme-primary), 0.3) !important;
}

.company-chip :deep(.v-chip__prepend) {
  margin-inline-end: 8px;
}

.company-chip :deep(.v-icon) {
  transition: transform 0.3s ease;
}

.company-chip:hover :deep(.v-icon) {
  transform: scale(1.1);
}

.v-theme--dark .company-chip:hover {
  box-shadow: 0 6px 20px rgba(var(--v-theme-primary), 0.6) !important;
}

.join-dialog-card .v-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.product-link {
  text-decoration: none;
  transition: color 0.2s;
}
.product-link .text-primary {
  color: rgb(var(--v-theme-primary));
}
.product-link:hover .text-primary {
  text-decoration: underline;
}

.request-tabs-bar {
  position: relative;
}
.refresh-requests-btn {
  margin-left: 12px;
  margin-right: 12px;
  align-self: center;
}
.refresh-btn-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #111;
  border-radius: 50%;
  width: 40px;
  height: 40px;
}

.compact-buttons-row {
  margin-bottom: 0 !important;
}

.compact-button-col {
  padding-top: 4px !important;
  padding-bottom: 4px !important;
}
</style>
