const adapters = {
  requests: adaptRequest,
  purchases: adaptPurchase,
  sales: adaptSale,
  receivedOffers: (record) => adaptOffer(record, '받은 오퍼'),
  sentOffers: (record) => adaptOffer(record, '보낸 오퍼'),
  receivedEstimates: (record) => adaptEstimate(record, '받은 견적'),
  sentEstimates: (record) => adaptEstimate(record, '보낸 견적'),
  chats: adaptChatRoom,
};

export const mypageViews = Object.freeze({
  profile: { title: '계정 개요', kicker: 'ACCOUNT', sources: [] },
  requests: {
    title: '내 구매 요청',
    kicker: 'REQUESTS',
    sources: [{ key: 'requests', endpoint: '/api/v1/requests/me' }],
  },
  purchases: {
    title: '구매 내역',
    kicker: 'PURCHASES',
    sources: [{ key: 'purchases', endpoint: '/api/v1/orders/purchases' }],
  },
  sales: {
    title: '판매 내역',
    kicker: 'SALES',
    sources: [{ key: 'sales', endpoint: '/api/v1/orders/sales' }],
  },
  offers: {
    title: '주고받은 오퍼',
    kicker: 'OFFERS',
    sources: [
      { key: 'receivedOffers', endpoint: '/api/v1/proposals/received' },
      { key: 'sentOffers', endpoint: '/api/v1/proposals/sent', sellerOnly: true },
      { key: 'receivedEstimates', endpoint: '/api/v1/estimates/received', sellerOnly: true },
      { key: 'sentEstimates', endpoint: '/api/v1/estimates/sent' },
    ],
  },
  chats: {
    title: '채팅',
    kicker: 'CHATS',
    sources: [{ key: 'chats', endpoint: '/api/v1/chat-rooms' }],
  },
});

export function adaptMypagePayload(sourceKey, payload) {
  const adapter = adapters[sourceKey];
  if (!adapter) throw new Error(`Unknown mypage source: ${sourceKey}`);
  return extractRecords(payload).map(adapter);
}

export function extractPage(payload) {
  return {
    page: numberOrNull(payload?.page ?? payload?.number),
    totalPages: numberOrNull(payload?.totalPages),
    totalElements: numberOrNull(payload?.totalElements),
  };
}

function extractRecords(payload) {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.items)) return payload.items;
  if (Array.isArray(payload?.data)) return payload.data;
  return payload ? [payload] : [];
}

// Domain DTO changes are intentionally absorbed by the adapters below.
function adaptRequest(record) {
  return viewRecord(
    `request-${value(record, 'requestId', 'id')}`,
    value(record, 'title', 'requestTitle', 'name') || '구매 요청',
    value(record, 'status', 'requestStatus'),
    value(record, 'description', 'deliveryAddress', 'message'),
    formatMeta(record, ['eventDate', 'requestedAt', 'quantity', 'budget']),
  );
}

function adaptPurchase(record) {
  return viewRecord(
    `purchase-${value(record, 'orderId', 'id')}`,
    value(record, 'title', 'requestTitle', 'productName') || '구매 내역',
    value(record, 'status', 'orderStatus'),
    value(record, 'sellerName', 'businessName', 'deliveryAddress'),
    formatMeta(record, ['orderedAt', 'eventDate', 'quantity', 'totalAmount']),
  );
}

function adaptSale(record) {
  return viewRecord(
    `sale-${value(record, 'orderId', 'id')}`,
    value(record, 'title', 'requestTitle', 'productName') || '판매 내역',
    value(record, 'status', 'orderStatus'),
    value(record, 'buyerName', 'customerName', 'deliveryAddress'),
    formatMeta(record, ['orderedAt', 'eventDate', 'quantity', 'totalAmount']),
  );
}

function adaptOffer(record, direction) {
  return viewRecord(
    `proposal-${value(record, 'proposalId', 'id')}-${direction}`,
    value(record, 'requestTitle', 'title') || direction,
    value(record, 'status', 'proposalStatus'),
    value(record, 'message', 'sellerName', 'buyerName'),
    [direction, formatMeta(record, ['quantity', 'totalAmount', 'proposedAt'])].filter(Boolean).join(' · '),
  );
}

function adaptEstimate(record, direction) {
  return viewRecord(
    `estimate-${value(record, 'estimateId', 'id')}-${direction}`,
    value(record, 'itemName', 'requestTitle', 'title') || direction,
    value(record, 'status', 'estimateStatus'),
    value(record, 'message', 'sellerName', 'buyerName'),
    [direction, formatMeta(record, ['quantity', 'budget', 'eventDateTime'])].filter(Boolean).join(' · '),
  );
}

function adaptChatRoom(record) {
  return viewRecord(
    `chat-${value(record, 'chatRoomId', 'id')}`,
    value(record, 'requestTitle', 'title', 'counterpartName') || '채팅방',
    value(record, 'status', 'chatStatus') || '진행 중',
    value(record, 'lastMessage', 'message'),
    formatMeta(record, ['counterpartName', 'lastMessageAt', 'updatedAt']),
  );
}

function viewRecord(key, title, status, detail, meta) {
  return {
    key,
    title,
    status: status || '진행 중',
    detail: detail || '',
    meta: meta || '',
  };
}

function value(record, ...keys) {
  for (const key of keys) {
    const candidate = record?.[key];
    if (candidate !== undefined && candidate !== null && candidate !== '') return String(candidate);
  }
  return '';
}

function formatMeta(record, keys) {
  return keys.map((key) => value(record, key)).filter(Boolean).join(' · ');
}

function numberOrNull(value) {
  return Number.isInteger(value) ? value : null;
}
