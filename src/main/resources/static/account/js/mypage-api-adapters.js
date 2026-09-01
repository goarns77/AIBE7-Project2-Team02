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
  const id = value(record, 'requestId', 'id');
  return viewRecord(
    `request-${id}`,
    value(record, 'title', 'requestTitle', 'name') || '구매 요청',
    value(record, 'status', 'requestStatus'),
    value(record, 'description', 'deliveryAddress', 'message'),
    joinMeta(
      labeledDate('행사', value(record, 'eventDateTime', 'eventDate')),
      labeled('수량', value(record, 'quantity'), '명'),
      labeledMoney('예산', value(record, 'budget')),
      labeled('카테고리', value(record, 'category')),
    ),
    id ? `/requests/${encodeURIComponent(id)}` : '',
    '주문 보기',
    value(record, 'eventDateTime', 'createdAt', 'requestedAt'),
  );
}

function adaptPurchase(record) {
  const id = value(record, 'orderId', 'id');
  return viewRecord(
    `purchase-${id}`,
    value(record, 'title', 'requestTitle', 'productName') || '구매 내역',
    value(record, 'status', 'orderStatus'),
    value(record, 'sellerName', 'businessName', 'deliveryAddress'),
    joinMeta(
      labeledDate('주문', value(record, 'orderedAt')),
      labeledDate('행사', value(record, 'eventDate', 'eventDateTime')),
      labeled('수량', value(record, 'quantity'), '명'),
      labeledMoney('결제', value(record, 'totalAmount')),
    ),
    '',
    '',
    value(record, 'orderedAt', 'eventDate', 'eventDateTime'),
  );
}

function adaptSale(record) {
  const id = value(record, 'orderId', 'id');
  return viewRecord(
    `sale-${id}`,
    value(record, 'title', 'requestTitle', 'productName') || '판매 내역',
    value(record, 'status', 'orderStatus'),
    value(record, 'buyerName', 'customerName', 'deliveryAddress'),
    joinMeta(
      labeledDate('주문', value(record, 'orderedAt')),
      labeledDate('행사', value(record, 'eventDate', 'eventDateTime')),
      labeled('수량', value(record, 'quantity'), '명'),
      labeledMoney('정산', value(record, 'totalAmount')),
    ),
    '',
    '',
    value(record, 'orderedAt', 'eventDate', 'eventDateTime'),
  );
}

function adaptOffer(record, direction) {
  const id = value(record, 'proposalId', 'id');
  const requestId = value(record, 'requestId');
  return viewRecord(
    `proposal-${id}-${direction}`,
    value(record, 'itemName', 'requestTitle', 'title') || `${direction} #${id}`,
    value(record, 'status', 'proposalStatus'),
    value(record, 'description', 'message', 'sellerName', 'buyerName'),
    joinMeta(
      direction,
      labeled('수량', value(record, 'quantity'), '명'),
      labeledMoney('1인 단가', value(record, 'unitPrice')),
      labeledMoney('총액', value(record, 'totalAmount')),
      labeledDate('제안', value(record, 'createdAt', 'proposedAt')),
    ),
    requestId ? `/requests/${encodeURIComponent(requestId)}` : '/proposals',
    requestId ? '주문 보기' : '제안 보기',
    value(record, 'createdAt', 'proposedAt'),
  );
}

function adaptEstimate(record, direction) {
  const id = value(record, 'estimateId', 'id');
  return viewRecord(
    `estimate-${id}-${direction}`,
    value(record, 'itemName', 'requestTitle', 'title') || `${direction} #${id}`,
    value(record, 'status', 'estimateStatus'),
    value(record, 'description', 'message', 'sellerName', 'buyerName'),
    joinMeta(
      direction,
      labeledMoney('예산', value(record, 'budget')),
      labeledDate('행사', value(record, 'eventDateTime')),
      labeledDate('요청', value(record, 'createdAt')),
    ),
    id ? `/estimates/${encodeURIComponent(id)}` : '/estimates',
    '견적 보기',
    value(record, 'createdAt', 'eventDateTime'),
  );
}

function adaptChatRoom(record) {
  const id = value(record, 'chatRoomId', 'id');
  const originType = value(record, 'originType');
  return viewRecord(
    `chat-${id}`,
    value(record, 'requestTitle', 'title', 'counterpartName')
      || `${originLabel(originType)} 채팅방 #${id}`,
    value(record, 'status', 'chatStatus') || '진행 중',
    value(record, 'lastMessage', 'message') || chatReference(record),
    joinMeta(
      labeled('유형', originLabel(originType)),
      labeledDate('개설', value(record, 'createdAt')),
    ),
    '',
    '',
    value(record, 'lastMessageAt', 'updatedAt', 'createdAt'),
  );
}

function viewRecord(key, title, status, detail, meta, href = '', actionLabel = '', sortValue = '') {
  return {
    key,
    title,
    status: statusLabel(status),
    detail: detail || '',
    meta: meta || '',
    href,
    actionLabel,
    sortAt: timestamp(sortValue),
  };
}

function value(record, ...keys) {
  for (const key of keys) {
    const candidate = record?.[key];
    if (candidate !== undefined && candidate !== null && candidate !== '') return String(candidate);
  }
  return '';
}

function joinMeta(...parts) {
  return parts.filter(Boolean).join(' · ');
}

function labeled(label, candidate, suffix = '') {
  return candidate ? `${label} ${candidate}${suffix}` : '';
}

function labeledMoney(label, candidate) {
  if (!candidate) return '';
  const amount = Number(candidate);
  return Number.isFinite(amount) ? `${label} ${amount.toLocaleString('ko-KR')}원` : `${label} ${candidate}`;
}

function labeledDate(label, candidate) {
  if (!candidate) return '';
  const date = new Date(candidate);
  return Number.isNaN(date.getTime()) ? `${label} ${candidate}` : `${label} ${date.toLocaleString('ko-KR')}`;
}

function timestamp(candidate) {
  if (!candidate) return 0;
  const time = new Date(candidate).getTime();
  return Number.isNaN(time) ? 0 : time;
}

function statusLabel(status) {
  const labels = {
    MATCHING: '매칭 중',
    IN_TALK: '협의 중',
    CONFIRMED: '확정',
    COMPLETED: '완료',
    CANCELED: '취소',
    CANCELLED: '취소',
    SENT: '전송됨',
    REQUESTED: '요청됨',
    ACCEPTED: '수락',
    REJECTED: '거절',
    WITHDRAWN: '철회',
    ACTIVE: '진행 중',
    CLOSED: '종료',
    PENDING: '대기',
    FAILED: '실패',
  };
  return labels[status] || status || '진행 중';
}

function originLabel(originType) {
  return {
    PROPOSAL: '제안 기반',
    INQUIRY: '문의',
  }[originType] || '거래';
}

function chatReference(record) {
  const references = [
    labeled('제안', value(record, 'proposalId') ? `#${value(record, 'proposalId')}` : ''),
    labeled('견적', value(record, 'quoteId') ? `#${value(record, 'quoteId')}` : ''),
  ];
  return references.filter(Boolean).join(' · ') || '연결된 거래 채팅입니다.';
}

function numberOrNull(value) {
  return Number.isInteger(value) ? value : null;
}
