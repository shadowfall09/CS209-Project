import { request } from '@/utils/request';
import type { ErrorAndException,BugListResult,Bug } from '@/api/model/bugModel';

const Api = {
  ErrorAndException: '/bug/ErrorAndException',
  SyntaxError: '/bug/SyntaxError/{limit}',
  FatalError: '/bug/FatalError/{limit}',
  Exception: '/bug/Exception/{limit}',
  Error: '/bug/Error/{limit}',
};

export function getErrorAndException() {
  return request.get<ErrorAndException>({
    url: Api.ErrorAndException,
  });
}

export function getSyntaxError(limit: number) {
  return request.get<BugListResult>({
    url: Api.SyntaxError.replace('{limit}', limit.toString()),
  });
}

export function getFatalError(limit: number) {
  return request.get<BugListResult>({
    url: Api.FatalError.replace('{limit}', limit.toString()),
  });
}

export function getException(limit: number) {
  return request.get<BugListResult>({
    url: Api.Exception.replace('{limit}', limit.toString()),
  });
}

export function getError(limit: number) {
  return request.get<BugListResult>({
    url: Api.Error.replace('{limit}', limit.toString()),
  });
}
