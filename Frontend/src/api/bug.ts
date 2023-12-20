import { request } from '@/utils/request';
import type { ErrorAndException } from '@/api/model/bugModel';

const Api = {
  ErrorAndException: '/bug/ErrorAndException',
};

export function getErrorAndException() {
  return request.get<ErrorAndException>({
    url: Api.ErrorAndException,
  });
}
