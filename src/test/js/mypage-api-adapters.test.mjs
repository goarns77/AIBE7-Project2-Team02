import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import test from 'node:test';

const source = await readFile(
  new URL('../../main/resources/static/account/js/mypage-api-adapters.js', import.meta.url),
  'utf8',
);
const adapters = await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`);

test('실제 주문 응답을 검색 가능한 마이페이지 레코드로 변환한다', () => {
  const [record] = adapters.adaptMypagePayload('requests', [{
    id: 7,
    title: '가을 행사 도시락',
    description: '한식 구성',
    status: 'MATCHING',
    eventDateTime: '2026-09-12T12:00:00',
    quantity: 20,
    budget: 300000,
    category: '한식',
  }]);

  assert.equal(record.href, '/requests/7');
  assert.equal(record.sourceLabel, '구매 요청');
  assert.equal(record.status, '매칭 중');
  assert.equal(record.statusCode, 'MATCHING');
  assert.match(record.meta, /수량 20명/);
});

test('제목과 출처를 한글 검색어로 필터링한다', () => {
  const records = [
    ...adapters.adaptMypagePayload('receivedOffers', [{id: 1, itemName: '샌드위치', status: 'SENT'}]),
    ...adapters.adaptMypagePayload('sentEstimates', [{id: 2, itemName: '도시락', status: 'REQUESTED'}]),
  ];

  assert.deepEqual(
    adapters.filterMypageRecords(records, '도시락').map(({key}) => key),
    ['estimate-2-보낸 견적'],
  );
  assert.deepEqual(
    adapters.filterMypageRecords(records, '받은 제안').map(({key}) => key),
    ['proposal-1-받은 오퍼'],
  );
});

test('원본 상태 코드로 목록을 필터링한다', () => {
  const records = [
    ...adapters.adaptMypagePayload('receivedOffers', [{id: 1, itemName: '샌드위치', status: 'ACCEPTED'}]),
    ...adapters.adaptMypagePayload('sentEstimates', [{id: 2, itemName: '도시락', status: 'REQUESTED'}]),
  ];

  const filtered = adapters.filterMypageRecords(records, '', 'ACCEPTED');

  assert.equal(filtered.length, 1);
  assert.equal(filtered[0].status, '수락');
});

test('판매 내역 소스는 판매자 역할에만 노출한다', () => {
  assert.equal(adapters.mypageViews.sales.sources[0].sellerOnly, true);
});
